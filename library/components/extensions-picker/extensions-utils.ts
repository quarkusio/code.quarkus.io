import { ExtensionEntry } from './extensions-picker';
import { Extension } from '../api/model';
import _ from 'lodash';

function* matchAll(str, regexp) {
  const flags = regexp.global ? regexp.flags : regexp.flags + 'g';
  const re = new RegExp(regexp, flags);
  let match;
  while (match = re.exec(str)) {
    yield match;
  }
}

type ExtensionFieldValueSupplier = (e: Extension) => string | string[] | undefined

interface ExtensionFieldIdentifier {
  keys: string[];
  valueSupplier: ExtensionFieldValueSupplier;
}

// FOR SHORTCUT KEYS, MAKE SURE IT IS AFTER THE FULL KEY (REPLACE IS TAKING THE FIRST)
const FIELD_IDENTIFIERS: ExtensionFieldIdentifier[] = [
  { keys: [ 'name' ], valueSupplier: e => e.name?.toLowerCase() },
  { keys: [ 'description', 'desc' ], valueSupplier: e => e.description?.toLowerCase() },
  { keys: [ 'groupid', 'group-id', 'group' ], valueSupplier: e => e.id?.toLowerCase().split(':')[0] },
  { keys: [ 'artifactid', 'artifact-id', 'artifact' ], valueSupplier: e => e.id?.toLowerCase().split(':')[1] },
  { keys: [ 'shortname', 'short-name' ], valueSupplier: e => e.shortName?.toLowerCase() },
  { keys: [ 'keywords', 'keyword' ], valueSupplier: e => e.keywords },
  { keys: [ 'tags', 'tag' ], valueSupplier: e => e.tags },
  { keys: [ 'category', 'cat' ], valueSupplier: e => e.category?.toLowerCase().replace(' ', '-') },
];

const FIELD_KEYS = FIELD_IDENTIFIERS.map(s => s.keys).reduce((acc, value) => acc.concat(value), [])

const getInPattern = keys => `(?<expr>([a-zA-Z0-9-._]+\\s+)*[a-zA-Z0-9-._]+)\\sin\\s(?<fields>((${keys.join('|')}),?)+)`;
const getInRegexp = keys => new RegExp(getInPattern(keys), 'gi');
const getEqualsPattern = keys => `(?<field>${keys.join('|')}):(?<expr>([a-zA-Z0-9-._,]+|("([a-zA-Z0-9-._,:]+\\s*)+")))`;
const getEqualsRegexp = keys => new RegExp(getEqualsPattern(keys), 'gi');
const ORIGIN_PATTERN = '\\s*origin:(?<origin>platform|other)\\s*'
const ORIGIN_REGEX = new RegExp(ORIGIN_PATTERN, 'gi');
const ORIGIN_REGEX_CLEAR = new RegExp(ORIGIN_PATTERN, 'i');

export interface ProcessedExtensions {
  extensionsValues: ExtensionValues[];
  fieldKeys: string[];
}

export interface ExtensionValues {
  extension: Extension;
  values: Map<string, string | string[] | undefined>;
}

export function getAllKeys(extensions: Extension[]): string[] {
  const keys = new Set<string>();
  FIELD_KEYS.forEach(k => keys.add(k));
  for (let extension of extensions) {
    for (let tag of extension.tags || []) {
      if (tag.indexOf(':') > 0) {
        keys.add(tag.split(':')[0])
      }
    }
  }
  return Array.from(keys);
}

export function processTags(tags: string[]): { [field: string]: string[] } {
  const processed: { [field: string]: string[] } = {};
  for (let tag of tags) {
    let key: string, value: string;
    if (tag.indexOf(':') > 0) {
      const s = tag.split(':');
      key = s[0];
      value = s[1];
    } else {
      key = 'tag';
      value = tag;
    }
    if(!processed[key]) {
      processed[key] = [];
    }
    processed[key].push(value) ;
  }
  return processed;
}

export function processExtensionsValues(extensions: Extension[]): ProcessedExtensions {
  const extensionsValues: ExtensionValues[] = [];
  const unique = removeDuplicateIds(extensions);
  for (let extension of unique) {
    const values = new Map<string, string | string[] | undefined>();
    for (let id of FIELD_IDENTIFIERS) {
      const val = id.valueSupplier(extension);
      for (let key of id.keys) {
        values.set(key, val);
      }
    }
    for (let tag of extension.tags || []) {
      if (tag.indexOf(':') > 0 && tag.indexOf('origin:') !== 0) {
        let pair = tag.split(':');
        const v = (values.get(pair[0]) || []) as string[];
        v.push(pair[1]);
        values.set(pair[0], v);
      }
    }
    const extensionValues = { extension, values };
    extensionsValues.push(extensionValues);
  }
  let fieldKeys = getAllKeys(extensions);
  return {
    extensionsValues,
    fieldKeys
  };
}

function inFilter(e: ExtensionValues, expr: string[], fields: string[]) {
  for (const field of fields) {
    const val = e.values.get(field);
    if (val) {
      console.log(`${field} ${val}==${expr}`);
      let allFoundInValue = true;
      for (const e of expr) {
        if (val.indexOf(e) < 0) {
          allFoundInValue = false;
          break;
        }
      }
      if (allFoundInValue) {
        return true
      }
    }
  }
  return false;
}

function equalsFilter(e: ExtensionValues, expr: string[], field: string) {
  const val = e.values.get(field);
  if (val) {
    for (const e of expr) {
      if (typeof val === 'string') {
        if (val === e) {
          return true;
        }
      } else if (val.indexOf(e) >= 0) {
        return true;
      }
    }
  }
  return false;
}

function defaultFiltering(filtered: ExtensionValues[], formattedSearch: string) {
  return filtered.filter(e => inFilter(e, formattedSearch.split(/\s+/), [ 'name', 'shortname', 'keywords', 'category' ]));
}


export function search(search: string, processedExtensions: ProcessedExtensions): Extension[] {
  let formattedSearch = search.trim().toLowerCase();
  if (!formattedSearch) {
    return processedExtensions.extensionsValues.map(v => v.extension);
  }
  let filtered = [ ...processedExtensions.extensionsValues ];
  const shortNameIndex = filtered.findIndex(e => e.values.get('shortname') === formattedSearch);
  if (shortNameIndex >= 0) {
    const val = filtered.splice(shortNameIndex, 1);
    filtered.unshift(val[0]);
  }
  // Basic search
  if(formattedSearch.indexOf(' in ') < 0 && formattedSearch.indexOf(':') < 0) {
    filtered = defaultFiltering(filtered, formattedSearch);
    return filtered.map(e => e.extension);
  }

  // Complex search
  const equalsRegex = getEqualsRegexp(processedExtensions.fieldKeys)
  const equalsMatches = matchAll(formattedSearch, equalsRegex);
  for (const e of equalsMatches) {
    if (!e.groups?.expr || !e.groups?.field) {
      continue;
    }

    const expr = e.groups.expr.replace(/"/g, '').split(',').map(s => s.toLowerCase().trim());
    const field = e.groups.field.trim().toLowerCase();
    filtered = filtered.filter(e => equalsFilter(e, expr, field));
  }

  formattedSearch = formattedSearch.replace(equalsRegex, ';').replace(ORIGIN_REGEX, ';').trim();

  if (formattedSearch) {
    const inRegex = getInRegexp(processedExtensions.fieldKeys)
    const inMatches = matchAll(formattedSearch, inRegex);
    for (const e of inMatches) {
      if (!e.groups?.expr || !e.groups?.fields) {
        continue;
      }
      const expr = e.groups.expr.split(/\s+/);
      const fields = e.groups.fields.split(/[\s,]+/);
      filtered = filtered.filter(e => inFilter(e, expr, fields));
    }
    formattedSearch = formattedSearch.replace(inRegex, '').replace(/;/g, '').trim();
    if (formattedSearch) {
      filtered = defaultFiltering(filtered, formattedSearch);
    }
  }
  return filtered.map(e => e.extension);
}

export const removeDuplicateIds = (entries: ExtensionEntry[]): ExtensionEntry[] => {
  return _.uniqBy(entries, 'id');
};

type Origin = 'other' | 'platform' | 'all';

export interface MetadataFilters {
  [key: string]: {
    active: string[];
    inactive: string[];
  }
}

export interface FilterResult {
  all: ExtensionEntry[];
  platform: ExtensionEntry[];
  other: ExtensionEntry[];
  selected: ExtensionEntry[];
  effective: ExtensionEntry[];
  origin: Origin;
  filters: MetadataFilters;
  filtered: boolean;
}

function getOrigin(filter: string): Origin {
  const originMatches = matchAll(filter, ORIGIN_REGEX);
  for (const e of originMatches) {
    if (e.groups?.origin) {
      return e.groups.origin as Origin;
    }
  }
  return 'all';
}

export function shouldFilter(filter: string): boolean {
  return clearFilterOrigin(filter).trim().length > 0;
}

export function clearFilterOrigin(filter: string) {
  return filter.replace(ORIGIN_REGEX_CLEAR, '');
}



function getMetadataFilters(filter: string, entries: ExtensionEntry[]): MetadataFilters {
  const tags = new Set<string>();
  const cats = new Set<string>();
  for (let entry of entries) {
    if (entry.tags) {
      for (let tag of entry.tags) {
        tags.add(tag);
      }
    }
    cats.add(entry.category?.toLowerCase().replace(' ', '-'))
  }
  const rawFilters = processTags(Array.from(tags));
  rawFilters.category = Array.from(cats);
  const filters: MetadataFilters = {};
  for (let key in rawFilters) {
    filters[key] = { active: [], inactive: [] };
    for (let val of rawFilters[key]) {
      if(filter.indexOf(key + ':' + val) >= 0) {
        filters[key].active.push(val);
      } else {
        filters[key].inactive.push(val);
      }
    }
  }
  return filters;
}

export function toFilterResult(filter: string, entries: Extension[], filtered: boolean, onResult: (result: FilterResult) => void) {
  const result: FilterResult = {
    all: entries,
    platform: [],
    other: [],
    origin: getOrigin(filter),
    selected: [],
    effective: [],
    filters: {},
    filtered
  }
  for (let entry of entries) {
    if (entry.platform) {
      result.platform.push(entry);
    } else {
      result.other.push(entry);
    }
  }
  result.selected = result[result.origin];
  result.effective = result.selected.length > 0 ? result.selected : result.all;
  result.filters = getMetadataFilters(filter, result.selected);
  onResult(result);
}

const computeResults = (filter: string, entries: ExtensionEntry[], processedExtensions: ProcessedExtensions, onResult: (result: FilterResult) => void): void => {
  if (shouldFilter(filter)) {
    const filtered = search(filter, processedExtensions);
    toFilterResult(filter, filtered, true, onResult);
  } else {
    toFilterResult(filter, entries, false, onResult);
  }
};


export const debouncedComputeResults = _.debounce(computeResults, 200);



import { ExtensionEntry } from './extensions-picker';
import { Extension } from '../api/model';
import _ from 'lodash';
const matchAll = require('string.prototype.matchall');

type ExtensionFieldValueSupplier = (e: Extension) => string | string[] | undefined

interface ExtensionFieldIdentifier {
  keys: string[];
  valueSupplier: ExtensionFieldValueSupplier;
  canSearch?: boolean;
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

const SEARCH_FIELD_KEYS = FIELD_IDENTIFIERS.filter(s => s.canSearch === undefined || s.canSearch).map(s => s.keys).reduce((acc, value) => acc.concat(value), [])
const FIELD_KEYS = FIELD_IDENTIFIERS.map(s => s.keys).reduce((acc, value) => acc.concat(value), [])

const IN_PATTERN = `(?<expr>(([a-zA-Z0-9-._]+)\\s*)+)\\sin\\s(?<fields>((${SEARCH_FIELD_KEYS.join('|')}),?)+)`;
const IN_REGEX = new RegExp(IN_PATTERN, 'gi');
const EQUALS_PATTERN = `(?<field>${FIELD_KEYS.join('|')}):(?<expr>([a-zA-Z0-9-._,]+|("([a-zA-Z0-9-._,]+\\s*)+")))`;
const EQUALS_REGEX = new RegExp(EQUALS_PATTERN, 'gi');
const ORIGIN_PATTERN = '\\s*origin:(?<origin>any|platform|other)\\s*'
const ORIGIN_REGEX = new RegExp(ORIGIN_PATTERN, 'gi');

export interface ExtensionValues {
  extension: Extension;
  values: Map<string, string | string[] | undefined>;
}

export function processExtensionsValues(extensions: Extension[]): ExtensionValues[] {
  const byFields:ExtensionValues[] = [];
  const unique = removeDuplicateIds(extensions);
  for (let extension of unique) {
    const values = new  Map<string, string | string[] | undefined>();
    for (let id of FIELD_IDENTIFIERS) {
      const val = id.valueSupplier(extension);
      for (let key of id.keys) {
        values.set(key, val);
      }
    }
    const extensionValues = { extension, values };
    byFields.push(extensionValues);
  }
  return byFields;
}

function inFilter(e: ExtensionValues, expr: string[], fields: string[]) {
  for (const field of fields) {
    const val = e.values.get(field);
    //console.log(`${field} ${val}==${expr}`);
    if (val) {
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

export function search(search: string, extensionValues: ExtensionValues[]): Extension[] {
  let formattedSearch = search.trim().toLowerCase();
  if (!formattedSearch) {
    return extensionValues.map(v => v.extension);
  }
  let filtered = [ ...extensionValues ];
  const shortNameIndex = filtered.findIndex(e => e.values.get('shortname') === formattedSearch);
  if (shortNameIndex >= 0) {
    const val = filtered.splice(shortNameIndex, 1);
    filtered.unshift(val[0]);
  }
  const equalsMatches = matchAll(formattedSearch, EQUALS_REGEX);
  for (const e of equalsMatches) {
    if (!e.groups?.expr || !e.groups?.field) {
      continue;
    }
    const expr = e.groups.expr.replace(/"/g, '').split(',').map(s => s.toLowerCase().trim());
    const field = e.groups.field.trim().toLowerCase();
    filtered = filtered.filter(e => equalsFilter(e, expr, field));
  }
  formattedSearch = formattedSearch.replace(EQUALS_REGEX, ';').replace(ORIGIN_REGEX, ';').trim();
  if(formattedSearch) {
    const inMatches = matchAll(formattedSearch, IN_REGEX);
    for (const e of inMatches) {
      if (!e.groups?.expr || !e.groups?.fields) {
        continue;
      }
      const expr = e.groups.expr.split(/\s+/);
      const fields = e.groups.fields.split(/[\s,]+/);
      filtered = filtered.filter(e => inFilter(e, expr, fields));
    }
    formattedSearch = formattedSearch.replace(IN_REGEX, '').replace(/;/g, '').trim();
    if (formattedSearch) {
      filtered = filtered.filter(e => inFilter(e, formattedSearch.split(/\s+/), [ 'name', 'shortname', 'keywords', 'tags', 'category' ]));
    }
  }
  return filtered.map(e => e.extension);
}

export const removeDuplicateIds = (entries: ExtensionEntry[]): ExtensionEntry[] => {
  return _.uniqBy(entries, 'id');
};

type Origin = 'any' | 'other' | 'platform';

export interface FilterResult {
  any: ExtensionEntry[];
  platform: ExtensionEntry[];
  other: ExtensionEntry[];
  selected: ExtensionEntry[];
  origin: Origin;
  metadata: {
    [key: string]: any
  };
  filtered: boolean;
}

function getOrigin(filter: string): Origin {
  const originMatches = matchAll(filter, ORIGIN_REGEX);
  for (const e of originMatches) {
    if (e.groups?.origin) {
      return e.groups.origin as Origin;
    }
  }
  return 'platform';
}

export function clearFilterOrigin(filter: string) {
  return filter.replace(ORIGIN_REGEX, '');
}

function getMetadata(entries: ExtensionEntry[]): { [key: string]: any } {
  const tags = new Set<string>();
  const categories = new Set<string>();
  for (let entry of entries) {
    if(entry.tags) {
      for(let tag of entry.tags) {
        tags.add(tag);
      }
    }
    categories.add(entry.category?.toLowerCase().replace(' ', '-'))
  }
  return { tags: Array.from(tags), categories: Array.from(categories) };
}

export function toFilterResult(filter: string, entries: Extension[], filtered: boolean, onResult: (result: FilterResult) => void) {
  const result: FilterResult = {
    any: entries,
    platform: [],
    other: [],
    origin: getOrigin(filter),
    selected: [],
    metadata:{},
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
  result.metadata = getMetadata(result.selected);
  onResult(result);
}

const directFilterEntries = (filter: string, extensions: ExtensionValues[], onResult: (result: FilterResult) => void): void => {
  const entries = search(filter, extensions);
  toFilterResult(filter, entries, true, onResult);
};



export const filterExtensions = _.debounce(directFilterEntries, 300);



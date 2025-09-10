import {ExtensionEntry} from './extensions-picker';
import {Extension} from '../api/model';
import _ from 'lodash';
import {Analytics} from '../../core/analytics';
import {parse, EqFilter, InFilter, TermFilter, Filter} from "../../core/search";

type ExtensionFieldValueSupplier = (e: Extension) => string | string[] | undefined

interface ExtensionFieldIdentifier {
  keys: string[];
  valueSupplier: ExtensionFieldValueSupplier;
}

const HIDE_FILTER_PREDICATE = (key: string) => [].includes(key);
const RADIO_FILTER_PREDICATE = (key: string) => ['platform', 'category'].includes(key);
const OPTIONAL_FILTER_PREDICATE = (key: string) => key === 'support' || key.endsWith('-support');

// FOR SHORTCUT KEYS, MAKE SURE IT IS AFTER THE FULL KEY (REPLACE IS TAKING THE FIRST)
const FIELD_IDENTIFIERS: ExtensionFieldIdentifier[] = [
  {keys: ['name'], valueSupplier: e => e.name?.toLowerCase()},
  {keys: ['description', 'desc'], valueSupplier: e => e.description?.toLowerCase()},
  {keys: ['groupid', 'group-id', 'group'], valueSupplier: e => e.id?.toLowerCase().split(':')[0]},
  {keys: ['artifactid', 'artifact-id', 'artifact'], valueSupplier: e => e.id?.toLowerCase().split(':')[1]},
  {keys: ['shortname', 'short-name'], valueSupplier: e => e.shortName?.toLowerCase()},
  {keys: ['keywords', 'keyword'], valueSupplier: e => e.keywords},
  {keys: ['tags', 'tag'], valueSupplier: e => e.tags},
  {keys: ['platform', 'p'], valueSupplier: e => e.platform ? 'yes' : 'no'},
  {keys: ['category', 'cat'], valueSupplier: e => catToId(e.category)},
];

const FIELD_KEYS = FIELD_IDENTIFIERS.map(s => s.keys).reduce((acc, value) => acc.concat(value), [])

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
    if (HIDE_FILTER_PREDICATE(key)) {
      continue;
    }
    if (!processed[key]) {
      processed[key] = [];
    }
    processed[key].push(value);
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
    const extensionValues = {extension, values};
    extensionsValues.push(extensionValues);
  }
  let fieldKeys = getAllKeys(extensions);
  return {
    extensionsValues,
    fieldKeys
  };
}

function filterIn(e: ExtensionValues, expr: string[], fields: string[]) {
  for (const field of fields) {
    const val = e.values.get(field.toLowerCase());
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

function filterEquals(extension: ExtensionValues, values: string[], field: string) {
  const extensionFieldValue = extension.values.get(field);
  if (!extensionFieldValue) return false;

  if (values.includes('*')) return true;
  for (const val of values) {
    const matches =
      typeof extensionFieldValue === 'string'
        ? extensionFieldValue === val
        : extensionFieldValue.indexOf(val) >= 0;
    if (matches) {
      return true;
    }
  }
  return false
}

function defaultFiltering(filtered: ExtensionValues[], term: string) {
  return filtered.filter(e => filterIn(e, [term], ['name', 'shortname', 'keywords', 'category', 'artifact-id']));
}

function parseQuery(query: string): Filter[] {
  const formattedSearch = query.trim().toLowerCase();
  if (!formattedSearch) {
    return [];
  }

  if (formattedSearch.indexOf(' in ') < 0 && formattedSearch.indexOf(':') < 0 && !formattedSearch.startsWith('-') && !formattedSearch.startsWith('!')) {
    return [{type: 'term', value: formattedSearch} as TermFilter];
  }
  try {
    return parse(formattedSearch);
  } catch (e: any) {
    console.log(e);
    return [];
  }
}

export function search(filters: Filter[], processedExtensions: ProcessedExtensions): Extension[] {
  if (!filters || filters.length === 0) {
    return processedExtensions.extensionsValues.map(v => v.extension);
  }
  let filtered = [...processedExtensions.extensionsValues];

  // Basic search
  if (filters.length === 1 && filters[0].type === 'term') {
    let f: TermFilter = filters[0];
    // We move any direct match to the top
    const shortNameIndex = filtered.findIndex(e => e.values.get('shortname') === f.value);
    if (shortNameIndex >= 0) {
      const val = filtered.splice(shortNameIndex, 1);
      filtered.unshift(val[0]);
    }

    // And then filter
    filtered = defaultFiltering(filtered, f.value);
    return filtered.map(e => e.extension);
  }

  // Advanced search
  for (let filter of filters) {
    switch (filter.type) {
      case 'eq':
        const eqFilter = filter as EqFilter;
        if (!processedExtensions.fieldKeys.includes(eqFilter.field.toLowerCase())) {
          continue;
        }
        filtered = filtered.filter(item => {
          const match = filterEquals(item, eqFilter.values, eqFilter.field);
          return eqFilter.negated ? !match : match;
        });
        break;
      case 'in':
        const inFilter = filter as InFilter;
        filtered = filtered.filter(item => filterIn(item, [inFilter.value], inFilter.fields))
        break;
      case 'term':
        const termFilter = filter as TermFilter;
        filtered = defaultFiltering(filtered, termFilter.value);
        break;
    }
  }

  return filtered.map(e => e.extension);
}

export const removeDuplicateIds = (entries: ExtensionEntry[]): ExtensionEntry[] => {
  return _.uniqBy(entries, 'id');
};

export interface MetadataFilterValues {
  radio: boolean;
  optional: boolean;
  exclude: boolean;
  any: boolean;
  all: { label: string, value: string; active: boolean; }[];
  active: string[];
  inactive: string[];
}

export interface MetadataFilters {
  [key: string]: MetadataFilterValues
}

export interface FilterResult {
  entries: ExtensionEntry[];
  filters: MetadataFilters;
  filtered: boolean;
}


export function shouldFilter(query: string): boolean {
  return query && query.length > 0;
}

export function addMetadataFilter(filters: MetadataFilters, query: string, key: string, value: string): string {
  if (filters[key].radio || filters[key].exclude || filters[key].any) {
    return (`${key}:${value} ` + clearMetadataFilter(query, key));
  }

  if (filters[key].inactive.length === 1 && filters[key].inactive[0] === value && filters[key].optional) {
    return addStarMetadataFilter(query, key);
  }
  if (filters[key].active.length > 0) {
    let split = query.split(new RegExp(`${key}:\\S+`, 'i'))
    return (split[0] + key + ':' + [...filters[key].active, value].join(',') + split[1]).trim();
  }
  return `${key}:${value} ${query}`.trim();
}

export function removeMetadataFilter(filters: MetadataFilters, query: string, key: string, value: string) {
  let active = filters[key].active;
  if (active.length <= 1) {
    return query.replace(`${key}:${value}`, '').trim();
  }
  let split = query.split(new RegExp(`${key}:\\S+`))
  active.splice(active.indexOf(value), 1);
  return (split[0] + key + ':' + active.join(',') + split[1]).trim();
}

export function clearMetadataFilter(query: string, key: string) {
  return query
    .replace(`-${key}`, '')
    .replace(`!${key}`, '')
    .replace(new RegExp(`${key}:\\S+`), '').trim();
}

export function addExcludeMetadataFilter(query: string, key: string) {
  return `!${key}` + query.replace(new RegExp(`${key}:\\S+`), '').trim();
}

export function addStarMetadataFilter(query: string, key: string) {
  return `${key}:* ` + query.replace(new RegExp(`${key}:\\S+`), '').trim();
}


function catToId(category?: string): string {
  return category?.toLowerCase().replace(' ', '-').replace(/\s+.+$/i, '');
}

function getMetadataFilters(filters: Filter[], entries: ExtensionEntry[]): MetadataFilters {
  const tags = new Set<string>();
  const cats = new Set<string>();
  for (let entry of entries) {
    if (entry.tags) {
      for (let tag of entry.tags) {
        tags.add(tag);
      }
    }
    cats.add(catToId(entry.category))
  }
  const tagFilters = processTags(Array.from(tags));
  tagFilters.category = Array.from(cats);
  tagFilters.platform = ['yes', 'no'];


  const metadataFilters: MetadataFilters = {};

  for (let key in tagFilters) {
    let filtersForTag = filters.filter(f => f.type === 'eq' && f.field === key) as EqFilter[];
    let filterForTag = filtersForTag?.length === 1 && filtersForTag[0];
    let any = filterForTag?.values?.includes('*') && !filterForTag.negated;
    let exclude = filterForTag?.values?.includes('*') && filterForTag.negated;
    metadataFilters[key] = {all: [], active: [], inactive: [], any, exclude,  radio: RADIO_FILTER_PREDICATE(key), optional: OPTIONAL_FILTER_PREDICATE(key)};
    for (let value of tagFilters[key]) {
      let label = value;
      let active = !filterForTag.negated && (filterForTag?.values?.includes(value) || any);

      if (active) {
        metadataFilters[key].all.push({value, label, active: true});
        metadataFilters[key].active.push(value);
      } else {
        metadataFilters[key].all.push({value, label, active: false});
        metadataFilters[key].inactive.push(value);
      }
    }

  }
  return metadataFilters;
}

export function toFilterResult(filters: Filter[], entries: Extension[], filteredEntries: Extension[], filtered: boolean, onResult: (result: FilterResult) => void) {
  const result: FilterResult = {
    entries: filteredEntries,
    filters: {},
    filtered
  }
  result.filters = getMetadataFilters(filters, entries);
  onResult(result);
}

const computeResults = (analytics: Analytics, query: string, entries: ExtensionEntry[], processedExtensions: ProcessedExtensions, onResult: (result: FilterResult) => void): void => {
  if (shouldFilter(query)) {
    analytics.event('Search', {filter: query, element: 'search-bar'})
    const filters = parseQuery(query);
    const filtered = search(filters, processedExtensions);
    toFilterResult(filters, entries, filtered, true, onResult);
  } else {
    toFilterResult([], entries, entries, false, onResult);
  }
};


export const debouncedComputeResults = _.debounce(computeResults, 200);



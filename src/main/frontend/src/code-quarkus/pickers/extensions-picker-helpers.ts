import { ExtensionEntry } from './extensions-picker';

export const filterFunction = (filter: string) => (d: ExtensionEntry) => {
  const filterLowerCase = filter.trim().toLowerCase();
  if (!filterLowerCase) {
    return true;
  }
  const shortName = d.shortName ? d.shortName.toLowerCase() : '';
  if (filterLowerCase === shortName) {
    return true;
  }
  return d.name.toLowerCase().includes(filterLowerCase)
    || d.keywords.filter(l => l.startsWith(filterLowerCase)).length > 0
    || (d.category && d.category.toLowerCase().startsWith(filterLowerCase))
    || shortName.startsWith(filterLowerCase);
}

export const sortFunction = (filter: string) => (a: ExtensionEntry, b: ExtensionEntry) => {
  const filterLowerCase = filter.trim().toLowerCase();
  if (!filterLowerCase) {
    return a.order > b.order ? 1 : -1;
  }
  const startWithAShortName = !!a.shortName && a.shortName.toLowerCase().startsWith(filterLowerCase);
  const startWithBShortName = !!b.shortName && b.shortName.toLowerCase().startsWith(filterLowerCase);
  if (startWithAShortName !== startWithBShortName) {
    return startWithAShortName ? -1 : 1;
  }
  const startWithOneOfALabel = a.keywords.filter(l => l.startsWith(filterLowerCase)).length > 0;
  const startWithOneOfBLabel = b.keywords.filter(l => l.startsWith(filterLowerCase)).length > 0;
  if (startWithOneOfALabel !== startWithOneOfBLabel) {
    return startWithOneOfALabel ? -1 : 1;
  }
  if (a.name.toLowerCase().startsWith(filterLowerCase) !== b.name.toLowerCase().startsWith(filterLowerCase)) {
    return a.name.toLowerCase().startsWith(filterLowerCase) ? -1 : 1;
  }
  return a.order > b.order ? 1 : -1;
}

export const removeDuplicateIds = (filter: string, entries: ExtensionEntry[]): ExtensionEntry[] => {
  if (!filter) {
    return entries;
  }
  const ids = new Set();
  return entries
    .filter(e => {
      if (ids.has(e.id)) {
        return false;
      }
      ids.add(e.id);
      return true;
    });
}

export const processEntries = (filter: string, entries: ExtensionEntry[]): ExtensionEntry[] => {
  return removeDuplicateIds(filter, entries.filter(filterFunction(filter)).sort(sortFunction(filter)));
}

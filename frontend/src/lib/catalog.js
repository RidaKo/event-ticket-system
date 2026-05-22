export const EMPTY_FILTERS = Object.freeze({
  categories: [],
  tags: [],
  startDate: "",
  endDate: "",
  location: "",
});

export function createEmptyFilters() {
  return {
    categories: [],
    tags: [],
    startDate: "",
    endDate: "",
    location: "",
  };
}

export function hasActiveFilters(filters) {
  return (
    filters.categories.length > 0 ||
    filters.tags.length > 0 ||
    Boolean(filters.startDate) ||
    Boolean(filters.endDate) ||
    Boolean(filters.location?.trim())
  );
}

/** Normalize filter values before sending to the API. */
export function normalizeFilters(filters) {
  let { startDate, endDate, categories, tags, location } = filters;

  if (startDate && endDate && endDate < startDate) {
    [startDate, endDate] = [endDate, startDate];
  }

  return {
    categories: [...categories],
    tags: [...tags],
    startDate,
    endDate,
    location: location.trim(),
  };
}

export function formatCategoryLabel(value) {
  if (!value) return "";
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export function buildCategoryLabelMap(categories = []) {
  return Object.fromEntries(categories.map((category) => [category.value, category.label]));
}

export const EMPTY_FILTERS = Object.freeze({
  categories: [],
  tags: [],
  startDate: "",
  endDate: "",
  location: "",
});

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

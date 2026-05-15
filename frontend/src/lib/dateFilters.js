/** Parse YYYY-MM-DD filter value into a local Date. */
export function parseFilterDate(value) {
  if (!value) return null;
  const [year, month, day] = value.split("-").map(Number);
  if (!year || !month || !day) return null;
  const date = new Date(year, month - 1, day);
  return Number.isNaN(date.getTime()) ? null : date;
}

/** Format a Date as YYYY-MM-DD for API filters. */
export function formatFilterDate(date) {
  if (!date) return "";
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/** Mantine range picker value from stored filter strings. */
export function filtersToDateRange(filters) {
  return [parseFilterDate(filters.startDate), parseFilterDate(filters.endDate)];
}

/** Stored filter strings from Mantine range picker value. */
export function dateRangeToFilters(range) {
  const [start, end] = range ?? [null, null];
  return {
    startDate: formatFilterDate(start),
    endDate: formatFilterDate(end),
  };
}

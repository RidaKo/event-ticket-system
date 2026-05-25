import { apiFetch } from "./client.js";

export async function getRecommendedEvents(filters = {}) {
  const params = new URLSearchParams();

  if (filters.categories?.length) {
    params.set("categories", filters.categories.join(","));
  }
  if (filters.tags?.length) {
    params.set("tags", filters.tags.join(","));
  }
  if (filters.startDate) {
    params.set("startDate", filters.startDate);
  }
  if (filters.endDate) {
    params.set("endDate", filters.endDate);
  }
  if (filters.location) {
    params.set("location", filters.location);
  }
  if (filters.limit) {
    params.set("limit", String(filters.limit));
  }
  if (filters.page != null) {
    params.set("page", String(filters.page));
  }
  if (filters.size) {
    params.set("size", String(filters.size));
  }

  const query = params.toString();
  return apiFetch(`/events/recommended${query ? `?${query}` : ""}`);
}

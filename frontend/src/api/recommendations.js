import { apiClient } from "./client.js";

/**
 * @typedef {"MUSIC"|"SPORTS"|"ARTS"|"TECHNOLOGY"|"FOOD"} Category
 *
 * @typedef {object} RecommendedEvent
 * @property {number} id
 * @property {string} title
 * @property {string} description
 * @property {string} startAt  ISO-8601 date-time string
 * @property {string|null} endAt
 * @property {string|null} city
 * @property {string|null} venue
 * @property {Category} category
 * @property {string[]} tags
 * @property {string|null} imageUrl
 *
 * @typedef {object} RecommendationResponse
 * @property {RecommendedEvent[]} items
 * @property {boolean} fallbackUsed
 *
 * @typedef {object} RecommendationFilters
 * @property {Category[]} [categories]
 * @property {string[]} [tags]       tag slugs (lowercase)
 * @property {string} [startDate]    ISO date (YYYY-MM-DD)
 * @property {string} [endDate]      ISO date (YYYY-MM-DD)
 * @property {string} [location]
 * @property {number} [limit]
 */

/**
 * @param {RecommendationFilters} [filters]
 * @returns {Promise<RecommendationResponse>}
 */
export async function getRecommendedEvents(filters = {}) {
  const params = {};
  if (filters.categories?.length) params.categories = filters.categories.join(",");
  if (filters.tags?.length) params.tags = filters.tags.join(",");
  if (filters.startDate) params.startDate = filters.startDate;
  if (filters.endDate) params.endDate = filters.endDate;
  if (filters.location) params.location = filters.location;
  if (filters.limit) params.limit = filters.limit;

  const { data } = await apiClient.get("/api/events/recommended", { params });
  return data;
}

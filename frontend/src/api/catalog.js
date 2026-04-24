import { apiClient } from "./client.js";

/**
 * @typedef {object} CategoryOption
 * @property {string} value
 * @property {string} label
 *
 * @typedef {object} TagOption
 * @property {number} id
 * @property {string} slug
 * @property {string} label
 *
 * @typedef {object} CatalogResponse
 * @property {CategoryOption[]} categories
 * @property {TagOption[]} tags
 */

/**
 * @returns {Promise<CategoryOption[]>}
 */
export async function getCategories() {
  const { data } = await apiClient.get("/api/categories");
  return data;
}

/**
 * @returns {Promise<TagOption[]>}
 */
export async function getTags() {
  const { data } = await apiClient.get("/api/tags");
  return data;
}

/**
 * @returns {Promise<CatalogResponse>}
 */
export async function getCatalog() {
  const [categories, tags] = await Promise.all([getCategories(), getTags()]);
  return { categories, tags };
}

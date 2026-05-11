import { apiFetch } from "./client.js";

export async function getCategories() {
  return apiFetch("/categories");
}

export async function getTags() {
  return apiFetch("/tags");
}

export async function getCatalog() {
  const [categories, tags] = await Promise.all([getCategories(), getTags()]);
  return { categories, tags };
}

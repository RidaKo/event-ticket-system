import { apiFetch } from "./client.js";

export async function getMyPreferences() {
  return apiFetch("/users/me/preferences");
}

export async function saveMyPreferences(preferences) {
  return apiFetch("/users/me/preferences", {
    method: "PUT",
    body: JSON.stringify({
      categorySlugs: preferences.categorySlugs ?? [],
      tagSlugs: preferences.tagSlugs ?? [],
      homeCity: preferences.homeCity?.trim() || null,
    }),
  });
}

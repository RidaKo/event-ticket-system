import {apiFetch} from "./client.js";

export function getCategories() {
    return apiFetch("/categories");
}
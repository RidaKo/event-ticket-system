import axios from "axios";

// Default baseURL is "" so requests go to "/api/..." on the dev server,
// where Vite's proxy (see vite.config.ts) forwards /api -> :8080. Override
// with VITE_API_URL when hitting the backend directly (e.g. preview builds).
const baseURL = import.meta.env.VITE_API_URL ?? "";

export const apiClient = axios.create({
  baseURL,
  timeout: 10_000,
});

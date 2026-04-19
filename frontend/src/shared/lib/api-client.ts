import { ApiError } from './api-error';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

type JsonBody = Record<string, unknown> | unknown[] | null;

interface RequestOptions {
  signal?: AbortSignal;
}

async function request<T>(
  method: string,
  path: string,
  body?: JsonBody,
  options: RequestOptions = {},
): Promise<T> {
  const init: RequestInit = {
    method,
    credentials: 'include',
    signal: options.signal,
    headers: {},
  };

  if (body !== undefined) {
    (init.headers as Record<string, string>)['content-type'] = 'application/json';
    init.body = JSON.stringify(body);
  }

  const response = await fetch(`${BASE_URL}${path}`, init);

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get('content-type') ?? '';
  const isJson = contentType.includes('application/json');
  const parsed = isJson ? await response.json().catch(() => undefined) : await response.text();

  if (!response.ok) {
    const message =
      (isJson && parsed && typeof parsed === 'object' && 'message' in parsed
        ? String((parsed as { message: unknown }).message)
        : typeof parsed === 'string' && parsed
          ? parsed
          : response.statusText) || `Request failed with status ${response.status}`;
    throw new ApiError(response.status, message, isJson ? parsed : undefined);
  }

  return parsed as T;
}

export const apiClient = {
  get: <T>(path: string, options?: RequestOptions) => request<T>('GET', path, undefined, options),
  post: <T = void>(path: string, body?: JsonBody, options?: RequestOptions) =>
    request<T>('POST', path, body, options),
  put: <T = void>(path: string, body?: JsonBody, options?: RequestOptions) =>
    request<T>('PUT', path, body, options),
  delete: <T = void>(path: string, options?: RequestOptions) =>
    request<T>('DELETE', path, undefined, options),
};

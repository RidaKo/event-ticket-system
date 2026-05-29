const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
const authStorageKey = 'eventTicket.auth';

export function readStoredAuth() {
  if (typeof window === 'undefined') {
    return null;
  }

  try {
    const value = window.localStorage.getItem(authStorageKey);
    return value ? JSON.parse(value) : null;
  } catch {
    return null;
  }
}

export function writeStoredAuth(auth) {
  if (typeof window === 'undefined') {
    return;
  }

  if (!auth) {
    window.localStorage.removeItem(authStorageKey);
    return;
  }

  window.localStorage.setItem(authStorageKey, JSON.stringify(auth));
}

export function clearStoredAuth() {
  writeStoredAuth(null);
}

export async function apiFetch(path, options = {}) {
  const {
    headers: customHeaders = {},
    orderToken,
    skipAuth = false,
    ...fetchOptions
  } = options;
  const headers = {
    'Content-Type': 'application/json',
    ...customHeaders
  };
  const storedAuth = readStoredAuth();

  if (!skipAuth && storedAuth?.token && !headers.Authorization) {
    headers.Authorization = `Bearer ${storedAuth.token}`;
  }

  if (orderToken && !headers['X-Order-Token']) {
    headers['X-Order-Token'] = orderToken;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers,
    ...fetchOptions
  });

  if (!response.ok) {
    let message = 'Request failed';
    let body = null;
    try {
      body = await response.json();
      message = body.detail || body.message || body.error || message;
    } catch {
      message = response.statusText || message;
    }
    const err = new Error(message);
    err.status = response.status;
    err.body = body;
    err.code = body?.code;
    err.current = body?.current;
    throw err;
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

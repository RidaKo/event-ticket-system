import { apiFetch } from './client';

export function login(payload) {
  return apiFetch('/auth/login', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({
      email: payload.email,
      password: payload.password
    })
  });
}

export function register(payload) {
  return apiFetch('/auth/register', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({
      fullName: payload.fullName,
      email: payload.email,
      password: payload.password,
      phone: payload.phone || null
    })
  });
}

export function getCurrentUser() {
  return apiFetch('/auth/me');
}

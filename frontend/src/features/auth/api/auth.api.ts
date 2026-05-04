import { apiClient } from '@/shared/lib/api-client';
import type { LoginRequest, User } from '../types';

export function login(body: LoginRequest): Promise<User> {
  return apiClient.post<User>('/auth/login', body);
}

export function logout(): Promise<void> {
  return apiClient.post<void>('/auth/logout');
}

export function fetchCurrentUser(): Promise<User> {
  return apiClient.get<User>('/auth/me');
}

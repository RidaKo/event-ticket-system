import { beforeEach, describe, expect, it } from 'vitest';
import { useAuthStore } from './auth.store';
import type { User } from '../types';

const user: User = {
  id: 'u1',
  username: 'ada',
  roles: ['USER'],
};

describe('useAuthStore', () => {
  beforeEach(() => {
    useAuthStore.getState().clear();
    localStorage.clear();
  });

  it('starts in idle or unauthenticated status with no user', () => {
    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(['idle', 'unauthenticated']).toContain(state.status);
  });

  it('setUser stores the user and marks authenticated', () => {
    useAuthStore.getState().setUser(user);
    expect(useAuthStore.getState().user).toEqual(user);
    expect(useAuthStore.getState().status).toBe('authenticated');
  });

  it('clear resets to unauthenticated', () => {
    useAuthStore.getState().setUser(user);
    useAuthStore.getState().clear();
    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().status).toBe('unauthenticated');
  });

  it('persists user to localStorage when set', () => {
    useAuthStore.getState().setUser(user);
    const raw = localStorage.getItem('auth');
    expect(raw).toBeTruthy();
    expect(raw!).toContain('"username":"ada"');
  });
});

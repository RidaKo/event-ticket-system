import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useCurrentUser, useLogin, useLogout } from './useAuth';
import { useAuthStore } from '../store/auth.store';

function makeWrapper() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false, gcTime: 0 }, mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
  };
}

describe('auth hooks', () => {
  beforeEach(() => {
    useAuthStore.getState().clear();
    localStorage.clear();
  });

  it('useLogin success writes user to store', async () => {
    const { result } = renderHook(() => useLogin(), { wrapper: makeWrapper() });

    await act(async () => {
      await result.current.mutateAsync({ username: 'ada', password: 'correct' });
    });

    expect(useAuthStore.getState().user?.username).toBe('ada');
    expect(useAuthStore.getState().status).toBe('authenticated');
  });

  it('useLogin failure surfaces an error and leaves store cleared', async () => {
    const { result } = renderHook(() => useLogin(), { wrapper: makeWrapper() });

    await act(async () => {
      await expect(
        result.current.mutateAsync({ username: 'ada', password: 'wrong' }),
      ).rejects.toMatchObject({ status: 401 });
    });

    expect(useAuthStore.getState().user).toBeNull();
  });

  it('useLogout clears the store', async () => {
    useAuthStore.getState().setUser({ id: 'u1', username: 'ada', roles: ['USER'] });
    const { result } = renderHook(() => useLogout(), { wrapper: makeWrapper() });

    await act(async () => {
      await result.current.mutateAsync();
    });

    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().status).toBe('unauthenticated');
  });

  it('useCurrentUser returns error when unauthenticated', async () => {
    const { result } = renderHook(() => useCurrentUser(), { wrapper: makeWrapper() });
    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(result.current.error).toMatchObject({ status: 401 });
  });
});

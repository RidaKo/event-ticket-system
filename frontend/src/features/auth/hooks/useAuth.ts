import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchCurrentUser, login, logout } from '../api/auth.api';
import { authKeys } from '../api/auth.keys';
import { useAuthStore } from '../store/auth.store';
import type { LoginRequest, User } from '../types';

export function useCurrentUser() {
  return useQuery<User>({
    queryKey: authKeys.me(),
    queryFn: fetchCurrentUser,
    retry: false,
  });
}

export function useLogin() {
  const queryClient = useQueryClient();
  const setUser = useAuthStore((s) => s.setUser);

  return useMutation({
    mutationFn: (body: LoginRequest) => login(body),
    onSuccess: (user) => {
      setUser(user);
      queryClient.setQueryData(authKeys.me(), user);
    },
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  const clear = useAuthStore((s) => s.clear);

  return useMutation({
    mutationFn: () => logout(),
    onSettled: () => {
      clear();
      queryClient.clear();
    },
  });
}

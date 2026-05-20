import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { getCurrentUser, login as loginRequest, register as registerRequest } from "../api/authApi.js";
import { clearStoredAuth, readStoredAuth, writeStoredAuth } from "../api/client.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => readStoredAuth());
  const [loading, setLoading] = useState(false);

  const storeAuth = useCallback((authResponse) => {
    const nextAuth = {
      token: authResponse.token,
      expiresAt: authResponse.expiresAt,
      user: authResponse.user,
    };
    writeStoredAuth(nextAuth);
    setAuth(nextAuth);
    return nextAuth;
  }, []);

  const logout = useCallback(() => {
    clearStoredAuth();
    setAuth(null);
  }, []);

  useEffect(() => {
    if (!auth?.token) {
      return undefined;
    }

    let active = true;
    setLoading(true);

    getCurrentUser()
      .then((user) => {
        if (!active) {
          return;
        }
        setAuth((current) => {
          if (!current?.token) {
            return current;
          }
          const nextAuth = { ...current, user };
          writeStoredAuth(nextAuth);
          return nextAuth;
        });
      })
      .catch(() => {
        if (active) {
          logout();
        }
      })
      .finally(() => active && setLoading(false));

    return () => {
      active = false;
    };
  }, [auth?.token, logout]);

  const login = useCallback(
    async (credentials) => {
      const authResponse = await loginRequest(credentials);
      return storeAuth(authResponse);
    },
    [storeAuth]
  );

  const register = useCallback(
    async (payload) => {
      const authResponse = await registerRequest(payload);
      return storeAuth(authResponse);
    },
    [storeAuth]
  );

  const value = useMemo(
    () => ({
      loading,
      token: auth?.token || null,
      user: auth?.user || null,
      isAuthenticated: Boolean(auth?.token),
      login,
      register,
      logout,
    }),
    [auth, loading, login, logout, register]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authService } from '../api/authService';
import { clearAuthStorage, getStoredToken, getStoredUser, setStoredToken, setStoredUser } from '../utils/storage';
import { normalizeRole } from '../utils/roles';

const AuthContext = createContext(null);

function decodeJwt(token) {
  if (!token || !token.includes('.')) return {};

  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const decoded = atob(payload);
    return JSON.parse(decoded);
  } catch {
    return {};
  }
}

function extractRoles(token, responseData) {
  if (responseData && Object.prototype.hasOwnProperty.call(responseData, 'roles')) {
    return normalizeRoles(responseData.roles);
  }

  const claims = decodeJwt(token);
  const candidates = [
    responseData?.authorities,
    responseData?.role,
    claims.roles,
    claims.authorities,
    claims.scope,
    claims.scopes,
  ].filter(Boolean);

  for (const candidate of candidates) {
    const roles = normalizeRoles(candidate);

    if (roles.length > 0) return roles;
  }

  return [];
}

function normalizeRoles(candidate) {
  const values = Array.isArray(candidate)
    ? candidate
    : String(candidate || '').split(/[,\s]+/).filter(Boolean);

  return values
    .map((value) => (typeof value === 'string' ? value : value?.authority || value?.name || value?.nombre))
    .map(normalizeRole)
    .filter(Boolean);
}

function buildUser(token, data) {
  const claims = decodeJwt(token);
  const roles = extractRoles(token, data);

  return {
    id: data?.userId || data?.id || claims.userId || claims.id || null,
    username: data?.username || claims.sub || claims.username || 'usuario',
    email: data?.email || claims.email || '',
    roles,
  };
}

function getInitialSession() {
  const storedToken = getStoredToken();
  const storedUser = getStoredUser();

  if (storedUser?.tokenRoleFallback) {
    clearAuthStorage();
    return { token: null, user: null };
  }

  return { token: storedToken, user: storedUser };
}

export function AuthProvider({ children }) {
  const [initialSession] = useState(getInitialSession);
  const [token, setToken] = useState(initialSession.token);
  const [user, setUser] = useState(initialSession.user);

  const logout = useCallback(() => {
    clearAuthStorage();
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => logout();
    window.addEventListener('farmared:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('farmared:unauthorized', handleUnauthorized);
  }, [logout]);

  const persistSession = useCallback((data) => {
    const sessionToken = data?.token;
    if (!sessionToken) {
      throw new Error('El backend no retorno token de autenticacion.');
    }

    const sessionUser = buildUser(sessionToken, data);
    setStoredToken(sessionToken);
    setStoredUser(sessionUser);
    setToken(sessionToken);
    setUser(sessionUser);
    return sessionUser;
  }, []);

  const login = useCallback(
    async (credentials) => {
      const result = await authService.login(credentials);
      const sessionUser = persistSession(result.data);
      return { ...result, user: sessionUser };
    },
    [persistSession]
  );

  const register = useCallback(
    async (payload) => {
      const result = await authService.register(payload);
      const sessionUser = persistSession(result.data);
      return { ...result, user: sessionUser };
    },
    [persistSession]
  );

  const roles = user?.roles || [];

  const value = useMemo(
    () => ({
      user,
      token,
      roles,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
      hasRole: (role) => roles.includes(role),
      hasAnyRole: (requiredRoles = []) => requiredRoles.some((role) => roles.includes(role)),
    }),
    [login, logout, register, roles, token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
}

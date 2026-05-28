const TOKEN_KEY = 'farmared_token';
const USER_KEY = 'farmared_user';

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function getStoredUser() {
  const value = localStorage.getItem(USER_KEY);
  if (!value) return null;

  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

export function setStoredUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuthStorage() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

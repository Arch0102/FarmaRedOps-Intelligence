import axiosClient, { getResult } from './axiosClient';

export const authService = {
  login(credentials) {
    return getResult(axiosClient.post('/auth/login', credentials));
  },

  register(payload) {
    return getResult(axiosClient.post('/auth/register', payload));
  },
};

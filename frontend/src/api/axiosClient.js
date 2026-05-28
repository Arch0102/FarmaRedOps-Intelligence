import axios from 'axios';
import { clearAuthStorage, getStoredToken } from '../utils/storage';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const axiosClient = axios.create({
  baseURL,
  timeout: 15000,
});

axiosClient.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const payload = error.response?.data;

    if (status === 401) {
      clearAuthStorage();
      window.dispatchEvent(new Event('farmared:unauthorized'));
      if (!window.location.pathname.includes('/login')) {
        window.location.assign('/login');
      }
    }

    const apiMessage = payload?.message;
    const errors = payload?.errors;
    let userMessage = apiMessage || 'No fue posible completar la solicitud.';

    if (status === 403) {
      userMessage = 'No tienes permisos para esta accion.';
    } else if (!error.response) {
      userMessage = 'No hay conexion con el backend. Verifica que el servidor este en ejecucion.';
    } else if (errors && typeof errors === 'object' && !Array.isArray(errors)) {
      userMessage = Object.values(errors).join(' ');
    }

    error.userMessage = userMessage;
    error.status = status;
    return Promise.reject(error);
  }
);

export function unwrapResponse(response) {
  const payload = response?.data;

  if (payload && typeof payload === 'object' && 'success' in payload && 'data' in payload) {
    return {
      data: payload.data,
      message: payload.message,
      raw: payload,
    };
  }

  return {
    data: payload,
    message: payload?.message || 'Operacion completada correctamente',
    raw: payload,
  };
}

export async function getResult(requestPromise) {
  const response = await requestPromise;
  return unwrapResponse(response);
}

export async function getData(requestPromise) {
  const result = await getResult(requestPromise);
  return result.data;
}

export default axiosClient;

import axiosClient, { getData, getResult } from './axiosClient';

export const dashboardService = {
  resumen() {
    return getData(axiosClient.get('/dashboard/resumen'));
  },

  metricas() {
    return getData(axiosClient.get('/dashboard/metricas'));
  },

  metricasPorTipo(tipo) {
    return getData(axiosClient.get(`/dashboard/metricas/tipo/${tipo}`));
  },

  recalcular() {
    return getResult(axiosClient.post('/dashboard/metricas/recalcular'));
  },
};

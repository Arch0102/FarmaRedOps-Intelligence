import axiosClient, { getData, getResult } from './axiosClient';

export const centroService = {
  list() {
    return getData(axiosClient.get('/centros-distribucion'));
  },

  getById(id) {
    return getData(axiosClient.get(`/centros-distribucion/${id}`));
  },

  create(payload) {
    return getResult(axiosClient.post('/centros-distribucion', payload));
  },

  update(id, payload) {
    return getResult(axiosClient.put(`/centros-distribucion/${id}`, payload));
  },

  remove(id) {
    return getResult(axiosClient.delete(`/centros-distribucion/${id}`));
  },
};

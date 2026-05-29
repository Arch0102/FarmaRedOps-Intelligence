import axiosClient, { getData, getResult } from './axiosClient';

export const medicamentoService = {
  list() {
    return getData(axiosClient.get('/medicamentos'));
  },

  getById(id) {
    return getData(axiosClient.get(`/medicamentos/${id}`));
  },

  create(payload) {
    return getResult(axiosClient.post('/medicamentos', payload));
  },

  update(id, payload) {
    return getResult(axiosClient.put(`/medicamentos/${id}`, payload));
  },

  remove(id) {
    return getResult(axiosClient.delete(`/medicamentos/${id}`));
  },
};

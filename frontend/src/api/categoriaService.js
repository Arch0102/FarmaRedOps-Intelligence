import axiosClient, { getData, getResult } from './axiosClient';

export const categoriaService = {
  list() {
    return getData(axiosClient.get('/categorias-medicamento'));
  },

  getById(id) {
    return getData(axiosClient.get(`/categorias-medicamento/${id}`));
  },

  create(payload) {
    return getResult(axiosClient.post('/categorias-medicamento', payload));
  },

  update(id, payload) {
    return getResult(axiosClient.put(`/categorias-medicamento/${id}`, payload));
  },

  remove(id) {
    return getResult(axiosClient.delete(`/categorias-medicamento/${id}`));
  },
};

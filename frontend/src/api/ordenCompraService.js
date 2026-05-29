import axiosClient, { getData, getResult } from './axiosClient';

export const ordenCompraService = {
  list() {
    return getData(axiosClient.get('/ordenes-compra'));
  },

  getById(id) {
    return getData(axiosClient.get(`/ordenes-compra/${id}`));
  },

  listByEstado(estado) {
    return getData(axiosClient.get(`/ordenes-compra/estado/${estado}`));
  },

  create(payload) {
    return getResult(axiosClient.post('/ordenes-compra', payload));
  },

  cambiarEstado(id, estado) {
    return getResult(axiosClient.patch(`/ordenes-compra/${id}/estado/${estado}`));
  },
};

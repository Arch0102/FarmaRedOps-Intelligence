import axiosClient, { getData, getResult } from './axiosClient';

export const proveedorService = {
  list() {
    return getData(axiosClient.get('/proveedores'));
  },

  listActivos() {
    return getData(axiosClient.get('/proveedores/activos'));
  },

  getById(id) {
    return getData(axiosClient.get(`/proveedores/${id}`));
  },

  create(payload) {
    return getResult(axiosClient.post('/proveedores', payload));
  },

  update(id, payload) {
    return getResult(axiosClient.put(`/proveedores/${id}`, payload));
  },

  desactivar(id) {
    return getResult(axiosClient.patch(`/proveedores/${id}/desactivar`));
  },
};

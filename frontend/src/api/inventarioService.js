import axiosClient, { getData, getResult } from './axiosClient';

export const inventarioService = {
  registrarMovimiento(payload) {
    return getResult(axiosClient.post('/movimientos-inventario', payload));
  },

  consultarMovimiento(id) {
    return getData(axiosClient.get(`/movimientos-inventario/${id}`));
  },

  consultarKardex(inventarioId) {
    return getData(axiosClient.get(`/movimientos-inventario/kardex/inventario/${inventarioId}`));
  },
};

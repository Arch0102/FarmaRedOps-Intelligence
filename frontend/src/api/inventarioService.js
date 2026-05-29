import axiosClient, { getData, getResult } from './axiosClient';

function unwrapApiData(response) {
  if (response?.data && typeof response.data === 'object' && 'data' in response.data) {
    return response.data.data;
  }
  return response?.data;
}

export const inventarioService = {
  async listarInventarios() {
    return unwrapApiData(await axiosClient.get('/inventarios')) || [];
  },

  async consultarInventario(id) {
    return unwrapApiData(await axiosClient.get(`/inventarios/${id}`));
  },

  async resumenInventario() {
    return unwrapApiData(await axiosClient.get('/inventarios/resumen'));
  },

  async listarLotes() {
    return unwrapApiData(await axiosClient.get('/lotes-medicamento')) || [];
  },

  async listarLotesPorMedicamento(medicamentoId) {
    return unwrapApiData(await axiosClient.get(`/lotes-medicamento/medicamento/${medicamentoId}`)) || [];
  },

  listarAlertasStock() {
    return getData(axiosClient.get('/alertas-stock'));
  },

  resolverAlertaStock(id) {
    return getResult(axiosClient.patch(`/alertas-stock/${id}/resolver`));
  },

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

import axiosClient, { getData, getResult } from './axiosClient';

export const documentoService = {
  list() {
    return getData(axiosClient.get('/documentos'));
  },

  getById(id) {
    return getData(axiosClient.get(`/documentos/${id}`));
  },

  upload(file, metadata) {
    const formData = new FormData();
    formData.append('archivo', file);
    formData.append(
      'metadata',
      new Blob([JSON.stringify(metadata)], { type: 'application/json' })
    );

    return getResult(
      axiosClient.post('/documentos/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
    );
  },

  update(id, payload) {
    return getResult(axiosClient.put(`/documentos/${id}`, payload));
  },

  remove(id) {
    return getResult(axiosClient.delete(`/documentos/${id}`));
  },

  async download(id) {
    return axiosClient.get(`/documentos/${id}/download`, { responseType: 'blob' });
  },

  async generateDashboardReport() {
    return axiosClient.post('/documentos/generar/dashboard', null, { responseType: 'blob' });
  },
};

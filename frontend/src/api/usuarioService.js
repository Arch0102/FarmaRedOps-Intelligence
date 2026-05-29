import axiosClient, { getData, getResult } from './axiosClient';

export const usuarioService = {
  listarUsuarios() {
    return getData(axiosClient.get('/usuarios'));
  },

  consultarUsuario(id) {
    return getData(axiosClient.get(`/usuarios/${id}`));
  },

  actualizarRoles(id, roles) {
    return getResult(axiosClient.put(`/usuarios/${id}/roles`, { roles }));
  },

  actualizarEstado(id, activo) {
    return getResult(axiosClient.patch(`/usuarios/${id}/estado`, { activo }));
  },

  list() {
    return this.listarUsuarios();
  },
};

export const ROLES = {
  AUXILIAR_BODEGA: 'ROLE_AUXILIAR_BODEGA',
  ANALISTA_COMPRAS: 'ROLE_ANALISTA_COMPRAS',
  ADMIN_AUDITOR: 'ROLE_ADMIN_AUDITOR',
};

export const ROLE_LABELS = {
  [ROLES.AUXILIAR_BODEGA]: 'Auxiliar de bodega',
  [ROLES.ANALISTA_COMPRAS]: 'Analista de compras',
  [ROLES.ADMIN_AUDITOR]: 'Admin auditor',
};

export const CATALOG_READ_ROLES = [
  ROLES.AUXILIAR_BODEGA,
  ROLES.ANALISTA_COMPRAS,
  ROLES.ADMIN_AUDITOR,
];

export const ADMIN_ROLES = [ROLES.ADMIN_AUDITOR];
export const PURCHASE_ROLES = [ROLES.ANALISTA_COMPRAS, ROLES.ADMIN_AUDITOR];
export const DASHBOARD_ROLES = [ROLES.ANALISTA_COMPRAS, ROLES.ADMIN_AUDITOR];
export const INVENTORY_WRITE_ROLES = [ROLES.AUXILIAR_BODEGA, ROLES.ADMIN_AUDITOR];

export function normalizeRole(role) {
  if (!role) return null;
  return role.startsWith('ROLE_') ? role : `ROLE_${role}`;
}

export function roleLabel(role) {
  return ROLE_LABELS[role] || role || 'Rol no disponible';
}

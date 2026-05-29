export const ROLES = {
  AUXILIAR_BODEGA: 'ROLE_AUXILIAR_BODEGA',
  ANALISTA_COMPRAS: 'ROLE_ANALISTA_COMPRAS',
  ADMIN_AUDITOR: 'ROLE_ADMIN_AUDITOR',
  USER: 'ROLE_USER',
};

export const ROLE_LABELS = {
  [ROLES.AUXILIAR_BODEGA]: 'Auxiliar de bodega',
  [ROLES.ANALISTA_COMPRAS]: 'Analista de compras',
  [ROLES.ADMIN_AUDITOR]: 'Admin auditor',
  [ROLES.USER]: 'Sin rol operativo',
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
export const OPERATIONAL_ROLES = [
  ROLES.AUXILIAR_BODEGA,
  ROLES.ANALISTA_COMPRAS,
  ROLES.ADMIN_AUDITOR,
];

export function normalizeRole(role) {
  if (!role) return null;
  return role.startsWith('ROLE_') ? role : `ROLE_${role}`;
}

export function roleLabel(role) {
  return ROLE_LABELS[role] || role || 'Rol no disponible';
}

export function normalizeRoles(roles = []) {
  const values = Array.isArray(roles)
    ? roles
    : String(roles || '').split(/[,\s]+/).filter(Boolean);

  return values.map(normalizeRole).filter(Boolean);
}

export function hasOperationalRole(roles = []) {
  return normalizeRoles(roles).some((role) => OPERATIONAL_ROLES.includes(role));
}

export function getDefaultRouteForRoles(roles = []) {
  const normalizedRoles = normalizeRoles(roles);

  if (normalizedRoles.includes(ROLES.ADMIN_AUDITOR) || normalizedRoles.includes(ROLES.ANALISTA_COMPRAS)) {
    return '/dashboard';
  }

  if (normalizedRoles.includes(ROLES.AUXILIAR_BODEGA)) {
    return '/medicamentos';
  }

  return '/forbidden';
}

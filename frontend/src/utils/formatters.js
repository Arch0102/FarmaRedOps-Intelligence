export function formatDate(value) {
  if (!value) return 'Sin fecha';
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  }).format(new Date(value));
}

export function formatDateTime(value) {
  if (!value) return 'Sin fecha';
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatCurrency(value) {
  const numericValue = Number(value || 0);
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(numericValue);
}

export function formatNumber(value) {
  return new Intl.NumberFormat('es-CO').format(Number(value || 0));
}

export function booleanLabel(value) {
  return value ? 'Activo' : 'Inactivo';
}

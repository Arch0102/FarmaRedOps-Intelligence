import { useEffect, useMemo, useState } from 'react';
import { Edit3, Power, RefreshCcw, ShieldCheck } from 'lucide-react';
import { usuarioService } from '../api/usuarioService';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import EmptyState from '../components/ui/EmptyState';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import Loading from '../components/ui/Loading';
import Modal from '../components/ui/Modal';
import Table from '../components/ui/Table';
import { formatDateTime, formatNumber } from '../utils/formatters';
import { ROLE_LABELS, ROLES, normalizeRole, roleLabel } from '../utils/roles';

const editableRoles = [
  ROLES.AUXILIAR_BODEGA,
  ROLES.ANALISTA_COMPRAS,
  ROLES.ADMIN_AUDITOR,
];

function normalizeRoles(roles = []) {
  return roles.map(normalizeRole).filter(Boolean);
}

function hasRole(roles, role) {
  return normalizeRoles(roles).includes(role);
}

export default function Usuarios() {
  const [rows, setRows] = useState([]);
  const [query, setQuery] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [loadError, setLoadError] = useState('');
  const [notice, setNotice] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    setLoadError('');
    try {
      const usuarios = await usuarioService.listarUsuarios();
      setRows(Array.isArray(usuarios) ? usuarios : []);
    } catch (exception) {
      const message = exception.userMessage || 'No fue posible cargar usuarios.';
      console.error('Error cargando usuarios', {
        status: exception.status,
        message,
        response: exception.response?.data,
      });
      setRows([]);
      setLoadError(message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const filteredRows = useMemo(() => {
    const value = query.toLowerCase();
    return rows.filter((item) =>
      [item.username, item.email, item.nombreCompleto, ...(item.roles || [])]
        .join(' ')
        .toLowerCase()
        .includes(value)
    );
  }, [query, rows]);

  const activeCount = useMemo(() => rows.filter((item) => item.activo).length, [rows]);
  const adminCount = useMemo(() => rows.filter((item) => item.activo && hasRole(item.roles, ROLES.ADMIN_AUDITOR)).length, [rows]);

  function openRoleEditor(row) {
    setSelectedUser(row);
    setSelectedRoles(normalizeRoles(row.roles).filter((role) => editableRoles.includes(role)));
    setModalOpen(true);
    setError('');
  }

  function toggleRole(role) {
    setSelectedRoles((current) =>
      current.includes(role)
        ? current.filter((item) => item !== role)
        : [...current, role]
    );
  }

  async function saveRoles(event) {
    event.preventDefault();
    if (!selectedUser) return;

    if (selectedRoles.length === 0) {
      setError('El usuario debe tener al menos un rol operativo.');
      return;
    }

    const confirmed = window.confirm(`Actualizar roles de ${selectedUser.username}?`);
    if (!confirmed) return;

    setSaving(true);
    setError('');
    setNotice('');
    try {
      const result = await usuarioService.actualizarRoles(selectedUser.id, selectedRoles);
      setNotice(result.message || 'Roles actualizados correctamente.');
      setModalOpen(false);
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible actualizar roles.');
    } finally {
      setSaving(false);
    }
  }

  async function toggleEstado(row) {
    const nextState = !row.activo;
    const action = nextState ? 'activar' : 'desactivar';
    const confirmed = window.confirm(`Confirmas ${action} al usuario ${row.username}?`);
    if (!confirmed) return;

    setSaving(true);
    setError('');
    setNotice('');
    try {
      const result = await usuarioService.actualizarEstado(row.id, nextState);
      setNotice(result.message || 'Estado actualizado correctamente.');
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible actualizar el estado.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <Loading text="Cargando usuarios y roles..." />;

  return (
    <div className="page-stack">
      <div className="page-title-row">
        <div>
          <h1>Usuarios y roles</h1>
          <p>Modulo administrativo reservado para ADMIN_AUDITOR.</p>
        </div>
        <Button variant="secondary" onClick={load}>
          <RefreshCcw size={16} />
          Actualizar
        </Button>
      </div>

      <ErrorMessage message={error || loadError} />
      {notice && <div className="success-message">{notice}</div>}

      <Card>
        <CardHeader
          title="Gestion de usuarios"
          subtitle="Consulta usuarios registrados, estados y roles operativos."
          meta={`${formatNumber(filteredRows.length)} de ${formatNumber(rows.length)} usuarios`}
        />
        <div className="toolbar-panel">
          <Input
            placeholder="Buscar por usuario, email, nombre o rol..."
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <div className="toolbar-actions">
            <span className="record-counter">{formatNumber(activeCount)} activos</span>
            <span className="record-counter">{formatNumber(adminCount)} admin activos</span>
          </div>
        </div>

        {loadError ? (
          <EmptyState
            title="No fue posible cargar usuarios"
            message="Revisa que el backend este corriendo y que el usuario tenga ROLE_ADMIN_AUDITOR. El detalle tecnico quedo en la consola."
          />
        ) : (
          <Table
            rows={filteredRows}
            compact
            columns={[
              { key: 'username', header: 'Usuario', render: (row) => <span className="code-chip">{row.username}</span> },
              { key: 'email', header: 'Email' },
              { key: 'nombreCompleto', header: 'Nombre completo' },
              {
                key: 'activo',
                header: 'Estado',
                render: (row) => <Badge tone={row.activo ? 'success' : 'neutral'}>{row.activo ? 'Activo' : 'Inactivo'}</Badge>,
              },
              {
                key: 'roles',
                header: 'Roles',
                render: (row) => (
                  <div className="row-actions">
                    {(row.roles || []).length ? (
                      row.roles.map((role) => (
                        <Badge key={role} tone={normalizeRole(role) === ROLES.ADMIN_AUDITOR ? 'info' : 'neutral'}>
                          {roleLabel(normalizeRole(role))}
                        </Badge>
                      ))
                    ) : (
                      <Badge tone="danger">Sin roles</Badge>
                    )}
                  </div>
                ),
              },
              { key: 'updatedAt', header: 'Actualizado', render: (row) => formatDateTime(row.updatedAt || row.createdAt) },
              {
                key: 'actions',
                header: 'Acciones',
                render: (row) => (
                  <div className="row-actions">
                    <Button variant="ghost" size="sm" onClick={() => openRoleEditor(row)}>
                      <Edit3 size={15} />
                      Roles
                    </Button>
                    <Button variant={row.activo ? 'danger' : 'secondary'} size="sm" onClick={() => toggleEstado(row)} disabled={saving}>
                      <Power size={15} />
                      {row.activo ? 'Desactivar' : 'Activar'}
                    </Button>
                  </div>
                ),
              },
            ]}
            emptyTitle="Sin usuarios"
            emptyMessage="No hay usuarios que coincidan con el filtro actual."
          />
        )}
      </Card>

      <Modal
        open={modalOpen}
        title="Actualizar roles"
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" form="roles-form" disabled={saving}>{saving ? 'Guardando...' : 'Guardar roles'}</Button>
          </>
        }
      >
        {selectedUser && (
          <form id="roles-form" className="detail-panel" onSubmit={saveRoles}>
            <div className="detail-summary">
              <div className="detail-summary-item"><span>Usuario</span><strong>{selectedUser.username}</strong></div>
              <div className="detail-summary-item"><span>Email</span><strong>{selectedUser.email}</strong></div>
              <div className="detail-summary-item"><span>Estado</span><Badge tone={selectedUser.activo ? 'success' : 'neutral'}>{selectedUser.activo ? 'Activo' : 'Inactivo'}</Badge></div>
              <div className="detail-summary-item"><span>Roles actuales</span><strong>{formatNumber((selectedUser.roles || []).length)}</strong></div>
            </div>

            <div className="info-list">
              {editableRoles.map((role) => (
                <label className="info-row role-option" key={role}>
                  <ShieldCheck size={18} />
                  <div>
                    <strong>{ROLE_LABELS[role]}</strong>
                    <span>{role}</span>
                  </div>
                  <input
                    type="checkbox"
                    checked={selectedRoles.includes(role)}
                    onChange={() => toggleRole(role)}
                    aria-label={`Asignar ${ROLE_LABELS[role]}`}
                  />
                </label>
              ))}
            </div>
            <p className="muted-note">
              El backend no permite dejar usuarios sin roles ni quitar el ultimo administrador activo.
            </p>
          </form>
        )}
      </Modal>
    </div>
  );
}

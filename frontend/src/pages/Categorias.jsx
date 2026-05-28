import { useEffect, useMemo, useState } from 'react';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { categoriaService } from '../api/categoriaService';
import { useAuth } from '../auth/AuthContext';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import Loading from '../components/ui/Loading';
import Modal from '../components/ui/Modal';
import Table from '../components/ui/Table';
import { ROLES } from '../utils/roles';

const emptyForm = { nombre: '', descripcion: '', activo: true };

export default function Categorias() {
  const { hasRole } = useAuth();
  const canManage = hasRole(ROLES.ADMIN_AUDITOR);
  const [rows, setRows] = useState([]);
  const [query, setQuery] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [editing, setEditing] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      setRows(await categoriaService.list());
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cargar categorias.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const filteredRows = useMemo(
    () => rows.filter((item) => item.nombre?.toLowerCase().includes(query.toLowerCase())),
    [query, rows]
  );

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEdit(row) {
    setEditing(row);
    setForm({
      nombre: row.nombre || '',
      descripcion: row.descripcion || '',
      activo: row.activo ?? true,
    });
    setModalOpen(true);
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const result = editing
        ? await categoriaService.update(editing.id, form)
        : await categoriaService.create(form);
      setNotice(result.message);
      setModalOpen(false);
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible guardar la categoria.');
    } finally {
      setSaving(false);
    }
  }

  async function remove(row) {
    if (!window.confirm(`Desactivar categoria ${row.nombre}?`)) return;
    try {
      const result = await categoriaService.remove(row.id);
      setNotice(result.message || 'Categoria desactivada.');
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No tienes permisos para esta accion.');
    }
  }

  if (loading) return <Loading />;

  return (
    <div className="page-stack">
      <div className="page-title-row">
        <div>
          <h1>Categorias</h1>
          <p>Clasificacion funcional del catalogo de medicamentos.</p>
        </div>
        {canManage && (
          <Button onClick={openCreate}>
            <Plus size={16} />
            Nueva categoria
          </Button>
        )}
      </div>
      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}
      <Card>
        <CardHeader title="Listado de categorias" action={<Input placeholder="Buscar categoria..." value={query} onChange={(event) => setQuery(event.target.value)} />} />
        <Table
          rows={filteredRows}
          columns={[
            { key: 'nombre', header: 'Nombre' },
            { key: 'descripcion', header: 'Descripcion' },
            { key: 'activo', header: 'Estado', render: (row) => <Badge tone={row.activo ? 'success' : 'neutral'}>{row.activo ? 'Activo' : 'Inactivo'}</Badge> },
            {
              key: 'actions',
              header: 'Acciones',
              render: (row) =>
                canManage ? (
                  <div className="row-actions">
                    <Button variant="ghost" size="sm" onClick={() => openEdit(row)}><Edit size={15} />Editar</Button>
                    <Button variant="danger" size="sm" onClick={() => remove(row)}><Trash2 size={15} />Desactivar</Button>
                  </div>
                ) : 'Solo lectura',
            },
          ]}
          emptyMessage="No hay categorias registradas."
        />
      </Card>

      <Modal
        open={modalOpen}
        title={editing ? 'Editar categoria' : 'Nueva categoria'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" form="categoria-form" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button>
          </>
        }
      >
        <form id="categoria-form" className="grid-form" onSubmit={save}>
          <Input label="Nombre" value={form.nombre} onChange={(event) => setForm((current) => ({ ...current, nombre: event.target.value }))} required />
          <Input label="Descripcion" value={form.descripcion} onChange={(event) => setForm((current) => ({ ...current, descripcion: event.target.value }))} />
          <label className="check-field">
            <input type="checkbox" checked={form.activo} onChange={(event) => setForm((current) => ({ ...current, activo: event.target.checked }))} />
            Categoria activa
          </label>
        </form>
      </Modal>
    </div>
  );
}

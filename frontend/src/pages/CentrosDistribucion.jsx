import { useEffect, useMemo, useState } from 'react';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { centroService } from '../api/centroService';
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

const emptyForm = { codigo: '', nombre: '', direccion: '', ciudad: '', activo: true };

export default function CentrosDistribucion() {
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
      setRows(await centroService.list());
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cargar centros.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const filteredRows = useMemo(() => {
    const value = query.toLowerCase();
    return rows.filter((item) => [item.codigo, item.nombre, item.ciudad].join(' ').toLowerCase().includes(value));
  }, [query, rows]);

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEdit(row) {
    setEditing(row);
    setForm({
      codigo: row.codigo || '',
      nombre: row.nombre || '',
      direccion: row.direccion || '',
      ciudad: row.ciudad || '',
      activo: row.activo ?? true,
    });
    setModalOpen(true);
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = { ...form };
      const result = editing ? await centroService.update(editing.id, payload) : await centroService.create(payload);
      setNotice(result.message);
      setModalOpen(false);
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible guardar el centro.');
    } finally {
      setSaving(false);
    }
  }

  async function remove(row) {
    if (!window.confirm(`Desactivar centro ${row.nombre}?`)) return;
    try {
      const result = await centroService.remove(row.id);
      setNotice(result.message || 'Centro desactivado.');
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
          <h1>Centros de distribucion</h1>
          <p>El codigo es un identificador operativo estable y no debe modificarse despues de creado.</p>
        </div>
        {canManage && <Button onClick={openCreate}><Plus size={16} />Nuevo centro</Button>}
      </div>
      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}
      <Card>
        <CardHeader title="Red de distribucion" action={<Input placeholder="Buscar centro..." value={query} onChange={(event) => setQuery(event.target.value)} />} />
        <Table
          rows={filteredRows}
          columns={[
            { key: 'codigo', header: 'Codigo' },
            { key: 'nombre', header: 'Nombre' },
            { key: 'ciudad', header: 'Ciudad' },
            { key: 'direccion', header: 'Direccion' },
            { key: 'activo', header: 'Estado', render: (row) => <Badge tone={row.activo ? 'success' : 'neutral'}>{row.activo ? 'Activo' : 'Inactivo'}</Badge> },
            {
              key: 'actions',
              header: 'Acciones',
              render: (row) => canManage ? <div className="row-actions"><Button variant="ghost" size="sm" onClick={() => openEdit(row)}><Edit size={15} />Editar</Button><Button variant="danger" size="sm" onClick={() => remove(row)}><Trash2 size={15} />Desactivar</Button></div> : 'Solo lectura',
            },
          ]}
          emptyMessage="No hay centros registrados."
        />
      </Card>

      <Modal open={modalOpen} title={editing ? 'Editar centro' : 'Nuevo centro'} onClose={() => setModalOpen(false)} footer={<><Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button><Button type="submit" form="centro-form" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button></>}>
        <form id="centro-form" className="grid-form" onSubmit={save}>
          <Input label="Codigo" value={form.codigo} onChange={(event) => setForm((current) => ({ ...current, codigo: event.target.value }))} required disabled={Boolean(editing)} />
          <Input label="Nombre" value={form.nombre} onChange={(event) => setForm((current) => ({ ...current, nombre: event.target.value }))} required />
          <Input label="Direccion" value={form.direccion} onChange={(event) => setForm((current) => ({ ...current, direccion: event.target.value }))} required />
          <Input label="Ciudad" value={form.ciudad} onChange={(event) => setForm((current) => ({ ...current, ciudad: event.target.value }))} required />
          <label className="check-field"><input type="checkbox" checked={form.activo} onChange={(event) => setForm((current) => ({ ...current, activo: event.target.checked }))} />Centro activo</label>
        </form>
      </Modal>
    </div>
  );
}

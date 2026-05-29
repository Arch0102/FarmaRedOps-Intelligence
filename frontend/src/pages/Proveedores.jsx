import { useEffect, useMemo, useState } from 'react';
import { Edit, Plus, Power } from 'lucide-react';
import { proveedorService } from '../api/proveedorService';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import Loading from '../components/ui/Loading';
import Modal from '../components/ui/Modal';
import Table from '../components/ui/Table';
import { formatNumber } from '../utils/formatters';

const emptyForm = { nit: '', nombre: '', telefono: '', correo: '', direccion: '', activo: true };

export default function Proveedores() {
  const [rows, setRows] = useState([]);
  const [query, setQuery] = useState('');
  const [onlyActive, setOnlyActive] = useState(false);
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
      setRows(onlyActive ? await proveedorService.listActivos() : await proveedorService.list());
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cargar proveedores.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [onlyActive]);

  const filteredRows = useMemo(() => {
    const value = query.toLowerCase();
    return rows.filter((item) => [item.nit, item.nombre, item.correo].join(' ').toLowerCase().includes(value));
  }, [query, rows]);
  const activeCount = useMemo(() => rows.filter((item) => item.activo).length, [rows]);

  function setField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEdit(row) {
    setEditing(row);
    setForm({
      nit: row.nit || '',
      nombre: row.nombre || '',
      telefono: row.telefono || '',
      correo: row.correo || '',
      direccion: row.direccion || '',
      activo: row.activo ?? true,
    });
    setModalOpen(true);
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const result = editing ? await proveedorService.update(editing.id, form) : await proveedorService.create(form);
      setNotice(result.message);
      setModalOpen(false);
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible guardar el proveedor.');
    } finally {
      setSaving(false);
    }
  }

  async function deactivate(row) {
    if (!window.confirm(`Desactivar proveedor ${row.nombre}?`)) return;
    try {
      const result = await proveedorService.desactivar(row.id);
      setNotice(result.message || 'Proveedor desactivado.');
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
          <h1>Proveedores</h1>
          <p>Gestion comercial de aliados de abastecimiento.</p>
        </div>
        <Button onClick={openCreate}><Plus size={16} />Nuevo proveedor</Button>
      </div>
      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}
      <Card>
        <CardHeader
          title="Directorio de proveedores"
          subtitle="Aliados de abastecimiento con datos comerciales y estado operativo."
          meta={`${formatNumber(filteredRows.length)} de ${formatNumber(rows.length)} registros`}
        />
        <div className="toolbar-panel">
          <Input placeholder="Buscar por NIT, nombre o correo..." value={query} onChange={(event) => setQuery(event.target.value)} />
          <div className="toolbar-actions">
            <span className="record-counter">{formatNumber(activeCount)} activos</span>
            <label className="check-field compact"><input type="checkbox" checked={onlyActive} onChange={(event) => setOnlyActive(event.target.checked)} />Solo activos</label>
          </div>
        </div>
        <Table
          rows={filteredRows}
          compact
          columns={[
            { key: 'nit', header: 'NIT', render: (row) => <span className="code-chip">{row.nit}</span> },
            { key: 'nombre', header: 'Nombre' },
            { key: 'telefono', header: 'Telefono' },
            { key: 'correo', header: 'Correo' },
            { key: 'activo', header: 'Estado', render: (row) => <Badge tone={row.activo ? 'success' : 'neutral'}>{row.activo ? 'Activo' : 'Inactivo'}</Badge> },
            { key: 'actions', header: 'Acciones', render: (row) => <div className="row-actions"><Button variant="ghost" size="sm" onClick={() => openEdit(row)}><Edit size={15} />Editar</Button><Button variant="danger" size="sm" onClick={() => deactivate(row)}><Power size={15} />Desactivar</Button></div> },
          ]}
          emptyTitle="Sin proveedores"
          emptyMessage="No hay proveedores que coincidan con el filtro actual."
        />
      </Card>
      <Modal open={modalOpen} title={editing ? 'Editar proveedor' : 'Nuevo proveedor'} onClose={() => setModalOpen(false)} footer={<><Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button><Button type="submit" form="proveedor-form" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button></>}>
        <form id="proveedor-form" className="grid-form" onSubmit={save}>
          <Input label="NIT" value={form.nit} onChange={(event) => setField('nit', event.target.value)} required />
          <Input label="Nombre" value={form.nombre} onChange={(event) => setField('nombre', event.target.value)} required />
          <Input label="Telefono" value={form.telefono} onChange={(event) => setField('telefono', event.target.value)} />
          <Input label="Correo" type="email" value={form.correo} onChange={(event) => setField('correo', event.target.value)} />
          <Input className="span-2" label="Direccion" value={form.direccion} onChange={(event) => setField('direccion', event.target.value)} />
          <label className="check-field"><input type="checkbox" checked={form.activo} onChange={(event) => setField('activo', event.target.checked)} />Proveedor activo</label>
        </form>
      </Modal>
    </div>
  );
}

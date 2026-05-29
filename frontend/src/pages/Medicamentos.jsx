import { useEffect, useMemo, useState } from 'react';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { categoriaService } from '../api/categoriaService';
import { medicamentoService } from '../api/medicamentoService';
import { useAuth } from '../auth/AuthContext';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import Loading from '../components/ui/Loading';
import Modal from '../components/ui/Modal';
import Select from '../components/ui/Select';
import Table from '../components/ui/Table';
import { formatNumber } from '../utils/formatters';
import { ROLES } from '../utils/roles';

const emptyForm = {
  codigo: '',
  nombre: '',
  descripcion: '',
  principioActivo: '',
  concentracion: '',
  presentacion: '',
  unidadMedida: '',
  stockMinimo: 0,
  stockMaximo: 1,
  puntoReorden: 0,
  activo: true,
  categoriaMedicamentoId: '',
};

export default function Medicamentos() {
  const { hasRole } = useAuth();
  const canManage = hasRole(ROLES.ADMIN_AUDITOR);
  const [rows, setRows] = useState([]);
  const [categorias, setCategorias] = useState([]);
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
      const [medicamentos, categoriasData] = await Promise.all([
        medicamentoService.list(),
        categoriaService.list().catch(() => []),
      ]);
      setRows(medicamentos || []);
      setCategorias(categoriasData || []);
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cargar medicamentos.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const filteredRows = useMemo(() => {
    const value = query.toLowerCase();
    return rows.filter((item) => [item.codigo, item.nombre, item.principioActivo, item.categoriaMedicamentoNombre].join(' ').toLowerCase().includes(value));
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
      codigo: row.codigo || '',
      nombre: row.nombre || '',
      descripcion: row.descripcion || '',
      principioActivo: row.principioActivo || '',
      concentracion: row.concentracion || '',
      presentacion: row.presentacion || '',
      unidadMedida: row.unidadMedida || '',
      stockMinimo: row.stockMinimo ?? 0,
      stockMaximo: row.stockMaximo ?? 1,
      puntoReorden: row.puntoReorden ?? 0,
      activo: row.activo ?? true,
      categoriaMedicamentoId: row.categoriaMedicamentoId || '',
    });
    setModalOpen(true);
  }

  function buildPayload() {
    return {
      ...form,
      stockMinimo: Number(form.stockMinimo),
      stockMaximo: Number(form.stockMaximo),
      puntoReorden: Number(form.puntoReorden),
      categoriaMedicamentoId: Number(form.categoriaMedicamentoId),
    };
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const result = editing
        ? await medicamentoService.update(editing.id, buildPayload())
        : await medicamentoService.create(buildPayload());
      setNotice(result.message);
      setModalOpen(false);
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible guardar el medicamento.');
    } finally {
      setSaving(false);
    }
  }

  async function remove(row) {
    if (!window.confirm(`Desactivar medicamento ${row.nombre}?`)) return;
    try {
      const result = await medicamentoService.remove(row.id);
      setNotice(result.message || 'Medicamento desactivado.');
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
          <h1>Medicamentos</h1>
          <p>Catalogo maestro de referencias farmaceuticas.</p>
        </div>
        {canManage && <Button onClick={openCreate}><Plus size={16} />Nuevo medicamento</Button>}
      </div>
      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}
      <Card>
        <CardHeader
          title="Catalogo de medicamentos"
          subtitle="Referencias disponibles para compras, inventario y documentos."
          meta={`${formatNumber(filteredRows.length)} de ${formatNumber(rows.length)} registros`}
        />
        <div className="toolbar-panel">
          <Input placeholder="Buscar por codigo, nombre, principio o categoria..." value={query} onChange={(event) => setQuery(event.target.value)} />
          <div className="toolbar-actions">
            <span className="record-counter">{formatNumber(activeCount)} activos</span>
          </div>
        </div>
        <Table
          rows={filteredRows}
          compact
          columns={[
            { key: 'codigo', header: 'Codigo', render: (row) => <span className="code-chip">{row.codigo}</span> },
            { key: 'nombre', header: 'Nombre' },
            { key: 'principioActivo', header: 'Principio activo' },
            { key: 'presentacion', header: 'Presentacion' },
            { key: 'categoriaMedicamentoNombre', header: 'Categoria' },
            { key: 'activo', header: 'Estado', render: (row) => <Badge tone={row.activo ? 'success' : 'neutral'}>{row.activo ? 'Activo' : 'Inactivo'}</Badge> },
            {
              key: 'actions',
              header: 'Acciones',
              render: (row) => canManage ? <div className="row-actions"><Button variant="ghost" size="sm" onClick={() => openEdit(row)}><Edit size={15} />Editar</Button><Button variant="danger" size="sm" onClick={() => remove(row)}><Trash2 size={15} />Desactivar</Button></div> : <Badge tone="neutral">Solo lectura</Badge>,
            },
          ]}
          emptyTitle="Sin medicamentos"
          emptyMessage="No hay medicamentos que coincidan con el filtro actual."
        />
      </Card>

      <Modal open={modalOpen} title={editing ? 'Editar medicamento' : 'Nuevo medicamento'} onClose={() => setModalOpen(false)} footer={<><Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button><Button type="submit" form="medicamento-form" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button></>}>
        <form id="medicamento-form" className="grid-form" onSubmit={save}>
          <Input label="Codigo" value={form.codigo} onChange={(event) => setField('codigo', event.target.value)} required />
          <Input label="Nombre" value={form.nombre} onChange={(event) => setField('nombre', event.target.value)} required />
          <Input label="Principio activo" value={form.principioActivo} onChange={(event) => setField('principioActivo', event.target.value)} required />
          <Input label="Concentracion" value={form.concentracion} onChange={(event) => setField('concentracion', event.target.value)} required />
          <Input label="Presentacion" value={form.presentacion} onChange={(event) => setField('presentacion', event.target.value)} required />
          <Input label="Unidad de medida" value={form.unidadMedida} onChange={(event) => setField('unidadMedida', event.target.value)} required />
          <Input label="Stock minimo" type="number" min="0" value={form.stockMinimo} onChange={(event) => setField('stockMinimo', event.target.value)} required />
          <Input label="Stock maximo" type="number" min="1" value={form.stockMaximo} onChange={(event) => setField('stockMaximo', event.target.value)} required />
          <Input label="Punto de reorden" type="number" min="0" value={form.puntoReorden} onChange={(event) => setField('puntoReorden', event.target.value)} required />
          <Select label="Categoria" value={form.categoriaMedicamentoId} onChange={(event) => setField('categoriaMedicamentoId', event.target.value)} required>
            <option value="">Seleccionar categoria</option>
            {categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nombre}</option>)}
          </Select>
          <Input className="span-2" label="Descripcion" value={form.descripcion} onChange={(event) => setField('descripcion', event.target.value)} />
          <label className="check-field"><input type="checkbox" checked={form.activo} onChange={(event) => setField('activo', event.target.checked)} />Medicamento activo</label>
        </form>
      </Modal>
    </div>
  );
}

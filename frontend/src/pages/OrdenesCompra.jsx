import { useEffect, useMemo, useState } from 'react';
import { Eye, Plus, RefreshCcw } from 'lucide-react';
import { medicamentoService } from '../api/medicamentoService';
import { ordenCompraService } from '../api/ordenCompraService';
import { proveedorService } from '../api/proveedorService';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import EmptyState from '../components/ui/EmptyState';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import Loading from '../components/ui/Loading';
import Modal from '../components/ui/Modal';
import Select from '../components/ui/Select';
import Table from '../components/ui/Table';
import { formatCurrency, formatDate, formatNumber } from '../utils/formatters';

const estados = ['BORRADOR', 'PENDIENTE', 'APROBADA', 'RECIBIDA', 'CANCELADA'];
const emptyForm = {
  codigo: '',
  proveedorId: '',
  fechaEstimadaEntrega: '',
  observacion: '',
  detalles: [{ medicamentoId: '', cantidad: 1, precioUnitario: 0 }],
};

function statusTone(estado) {
  if (estado === 'RECIBIDA') return 'success';
  if (estado === 'CANCELADA') return 'danger';
  if (estado === 'PENDIENTE') return 'warning';
  return 'info';
}

export default function OrdenesCompra() {
  const [rows, setRows] = useState([]);
  const [proveedores, setProveedores] = useState([]);
  const [medicamentos, setMedicamentos] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [modalOpen, setModalOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('');
  const [query, setQuery] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [ordenes, proveedoresData, medicamentosData] = await Promise.all([
        estadoFilter ? ordenCompraService.listByEstado(estadoFilter) : ordenCompraService.list(),
        proveedorService.listActivos().catch(() => []),
        medicamentoService.list().catch(() => []),
      ]);
      setRows(ordenes || []);
      setProveedores(proveedoresData || []);
      setMedicamentos(medicamentosData || []);
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cargar ordenes de compra.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [estadoFilter]);

  const totalPreview = useMemo(
    () => form.detalles.reduce((sum, item) => sum + Number(item.cantidad || 0) * Number(item.precioUnitario || 0), 0),
    [form.detalles]
  );

  const filteredRows = useMemo(() => {
    const value = query.toLowerCase();
    return rows.filter((item) => [item.codigo, item.proveedorNombre, item.estado].join(' ').toLowerCase().includes(value));
  }, [query, rows]);

  function updateDetail(index, field, value) {
    setForm((current) => ({
      ...current,
      detalles: current.detalles.map((item, itemIndex) => (itemIndex === index ? { ...item, [field]: value } : item)),
    }));
  }

  function addDetail() {
    setForm((current) => ({
      ...current,
      detalles: [...current.detalles, { medicamentoId: '', cantidad: 1, precioUnitario: 0 }],
    }));
  }

  function removeDetail(index) {
    setForm((current) => ({
      ...current,
      detalles: current.detalles.filter((_, itemIndex) => itemIndex !== index),
    }));
  }

  function buildPayload() {
    return {
      codigo: form.codigo,
      proveedorId: Number(form.proveedorId),
      fechaEstimadaEntrega: form.fechaEstimadaEntrega || null,
      observacion: form.observacion,
      detalles: form.detalles.map((item) => ({
        medicamentoId: Number(item.medicamentoId),
        cantidad: Number(item.cantidad),
        precioUnitario: Number(item.precioUnitario),
      })),
    };
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const result = await ordenCompraService.create(buildPayload());
      setNotice(result.message || 'Orden creada correctamente.');
      setModalOpen(false);
      setForm(emptyForm);
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible crear la orden.');
    } finally {
      setSaving(false);
    }
  }

  async function viewOrder(row) {
    setError('');
    try {
      setSelectedOrder(await ordenCompraService.getById(row.id));
      setDetailOpen(true);
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible consultar la orden.');
    }
  }

  async function changeState(row, estado) {
    try {
      const result = await ordenCompraService.cambiarEstado(row.id, estado);
      setNotice(result.message || 'Estado actualizado.');
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cambiar el estado.');
    }
  }

  if (loading) return <Loading />;

  return (
    <div className="page-stack">
      <div className="page-title-row">
        <div>
          <h1>Ordenes de compra</h1>
          <p>Gestion de abastecimiento con proveedor, estados y detalle.</p>
        </div>
        <Button onClick={() => setModalOpen(true)}><Plus size={16} />Nueva orden</Button>
      </div>
      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}
      <Card>
        <CardHeader
          title="Compras"
          subtitle="Ordenes emitidas con proveedor, estado y valor total."
          meta={`${formatNumber(filteredRows.length)} de ${formatNumber(rows.length)} ordenes`}
        />
        <div className="toolbar-panel">
          <Input placeholder="Buscar por codigo, proveedor o estado..." value={query} onChange={(event) => setQuery(event.target.value)} />
          <div className="toolbar-actions">
            <Select value={estadoFilter} onChange={(event) => setEstadoFilter(event.target.value)}>
              <option value="">Todos los estados</option>
              {estados.map((estado) => <option key={estado} value={estado}>{estado}</option>)}
            </Select>
          </div>
        </div>
        <Table
          rows={filteredRows}
          compact
          columns={[
            { key: 'codigo', header: 'Codigo', render: (row) => <span className="code-chip">{row.codigo}</span> },
            { key: 'proveedorNombre', header: 'Proveedor' },
            { key: 'estado', header: 'Estado', render: (row) => <Badge tone={statusTone(row.estado)}>{row.estado}</Badge> },
            { key: 'fechaOrden', header: 'Fecha', render: (row) => formatDate(row.fechaOrden) },
            { key: 'total', header: 'Total', align: 'right', render: (row) => formatCurrency(row.total) },
            { key: 'actions', header: 'Acciones', render: (row) => <div className="row-actions"><Button variant="ghost" size="sm" onClick={() => viewOrder(row)}><Eye size={15} />Detalle</Button><Select value="" onChange={(event) => event.target.value && changeState(row, event.target.value)}><option value="">Cambiar estado</option>{estados.filter((estado) => estado !== row.estado).map((estado) => <option key={estado} value={estado}>{estado}</option>)}</Select></div> },
          ]}
          emptyTitle="Sin ordenes"
          emptyMessage="No hay ordenes que coincidan con los filtros actuales."
        />
      </Card>

      <Modal open={modalOpen} title="Nueva orden de compra" onClose={() => setModalOpen(false)} footer={<><Button variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button><Button type="submit" form="orden-form" disabled={saving}>{saving ? 'Creando...' : 'Crear orden'}</Button></>}>
        <form id="orden-form" className="grid-form" onSubmit={save}>
          <Input label="Codigo" value={form.codigo} onChange={(event) => setForm((current) => ({ ...current, codigo: event.target.value }))} required />
          <Select label="Proveedor" value={form.proveedorId} onChange={(event) => setForm((current) => ({ ...current, proveedorId: event.target.value }))} required>
            <option value="">Seleccionar proveedor</option>
            {proveedores.map((proveedor) => <option key={proveedor.id} value={proveedor.id}>{proveedor.nombre}</option>)}
          </Select>
          <Input label="Fecha estimada" type="date" value={form.fechaEstimadaEntrega} onChange={(event) => setForm((current) => ({ ...current, fechaEstimadaEntrega: event.target.value }))} />
          <Input label="Observacion" value={form.observacion} onChange={(event) => setForm((current) => ({ ...current, observacion: event.target.value }))} />
          <div className="span-2 detail-editor">
            <div className="section-row">
              <h3>Detalles</h3>
              <Button variant="secondary" size="sm" onClick={addDetail}>Agregar detalle</Button>
            </div>
            {form.detalles.map((detalle, index) => (
              <div className="detail-line" key={index}>
                <Select value={detalle.medicamentoId} onChange={(event) => updateDetail(index, 'medicamentoId', event.target.value)} required>
                  <option value="">Medicamento</option>
                  {medicamentos.map((medicamento) => <option key={medicamento.id} value={medicamento.id}>{medicamento.nombre}</option>)}
                </Select>
                <Input type="number" min="1" value={detalle.cantidad} onChange={(event) => updateDetail(index, 'cantidad', event.target.value)} required />
                <Input type="number" min="0.01" step="0.01" value={detalle.precioUnitario} onChange={(event) => updateDetail(index, 'precioUnitario', event.target.value)} required />
                <Button variant="ghost" size="sm" onClick={() => removeDetail(index)} disabled={form.detalles.length === 1}>Quitar</Button>
              </div>
            ))}
            <strong>Total estimado: {formatCurrency(totalPreview)}</strong>
          </div>
        </form>
      </Modal>

      <Modal open={detailOpen} title="Detalle de orden" onClose={() => setDetailOpen(false)}>
        {selectedOrder ? (
          <div className="detail-panel">
            <div className="detail-summary">
              <div className="detail-summary-item"><span>Codigo</span><strong>{selectedOrder.codigo}</strong></div>
              <div className="detail-summary-item"><span>Proveedor</span><strong>{selectedOrder.proveedorNombre}</strong></div>
              <div className="detail-summary-item"><span>Estado</span><Badge tone={statusTone(selectedOrder.estado)}>{selectedOrder.estado}</Badge></div>
              <div className="detail-summary-item"><span>Total</span><strong>{formatCurrency(selectedOrder.total)}</strong></div>
            </div>
            <Table
              rows={selectedOrder.detalles || []}
              compact
              columns={[
                { key: 'medicamentoNombre', header: 'Medicamento' },
                { key: 'cantidad', header: 'Cantidad', align: 'right', render: (row) => formatNumber(row.cantidad) },
                { key: 'precioUnitario', header: 'Precio', align: 'right', render: (row) => formatCurrency(row.precioUnitario) },
                { key: 'subtotal', header: 'Subtotal', align: 'right', render: (row) => formatCurrency(row.subtotal) },
              ]}
            />
          </div>
        ) : <EmptyState />}
      </Modal>
    </div>
  );
}

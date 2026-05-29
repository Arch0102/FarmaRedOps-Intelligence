import { useState } from 'react';
import { Search, Send } from 'lucide-react';
import { inventarioService } from '../api/inventarioService';
import { useAuth } from '../auth/AuthContext';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import EmptyState from '../components/ui/EmptyState';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import Table from '../components/ui/Table';
import { formatDateTime, formatNumber } from '../utils/formatters';
import { INVENTORY_WRITE_ROLES } from '../utils/roles';

const emptyMovement = {
  tipoMovimiento: 'ENTRADA',
  inventarioId: '',
  loteMedicamentoId: '',
  cantidad: 1,
  motivo: '',
  observacion: '',
  usuarioResponsable: '',
};

function formatMovement(value) {
  return String(value || '').replaceAll('_', ' ');
}

function movementTone(value) {
  return value?.includes('ENTRADA') || value?.includes('POSITIVO') ? 'success' : 'warning';
}

export default function Inventario() {
  const { hasAnyRole, user } = useAuth();
  const canRegister = hasAnyRole(INVENTORY_WRITE_ROLES);
  const [kardexId, setKardexId] = useState('');
  const [kardex, setKardex] = useState([]);
  const [movement, setMovement] = useState({ ...emptyMovement, usuarioResponsable: user?.username || '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  function setField(field, value) {
    setMovement((current) => ({ ...current, [field]: value }));
  }

  async function buscarKardex(event) {
    event.preventDefault();
    if (!kardexId) return;
    setLoading(true);
    setError('');
    try {
      setKardex(await inventarioService.consultarKardex(kardexId));
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible consultar el kardex.');
    } finally {
      setLoading(false);
    }
  }

  async function registrar(event) {
    event.preventDefault();
    setError('');
    setNotice('');
    try {
      const payload = {
        ...movement,
        inventarioId: Number(movement.inventarioId),
        loteMedicamentoId: Number(movement.loteMedicamentoId),
        cantidad: Number(movement.cantidad),
      };
      const result = await inventarioService.registrarMovimiento(payload);
      setNotice(result.message || 'Movimiento registrado correctamente.');
      setMovement({ ...emptyMovement, usuarioResponsable: user?.username || '' });
      if (kardexId) await buscarKardex({ preventDefault: () => {} });
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible registrar el movimiento.');
    }
  }

  return (
    <div className="page-stack">
      <div className="page-title-row">
        <div>
          <h1>Inventario y movimientos</h1>
          <p>Registro de entradas, salidas y consulta de kardex por inventario.</p>
        </div>
      </div>
      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}

      <div className="two-column-grid">
        <Card>
          <CardHeader
            title="Registrar movimiento"
            subtitle={canRegister ? 'Entrada, salida o ajuste contra inventario y lote reales.' : 'No tienes permisos para registrar movimientos.'}
            meta={canRegister ? 'Operacion controlada' : 'Solo consulta'}
          />
          {canRegister ? (
            <form className="grid-form single" onSubmit={registrar}>
              <Select label="Tipo" value={movement.tipoMovimiento} onChange={(event) => setField('tipoMovimiento', event.target.value)}>
                <option value="ENTRADA">ENTRADA</option>
                <option value="SALIDA">SALIDA</option>
                <option value="AJUSTE_POSITIVO">AJUSTE_POSITIVO</option>
                <option value="AJUSTE_NEGATIVO">AJUSTE_NEGATIVO</option>
              </Select>
              <Input label="Inventario ID" type="number" value={movement.inventarioId} onChange={(event) => setField('inventarioId', event.target.value)} required />
              <Input label="Lote medicamento ID" type="number" value={movement.loteMedicamentoId} onChange={(event) => setField('loteMedicamentoId', event.target.value)} required />
              <Input label="Cantidad" type="number" min="1" value={movement.cantidad} onChange={(event) => setField('cantidad', event.target.value)} required />
              <Input label="Motivo" value={movement.motivo} onChange={(event) => setField('motivo', event.target.value)} required />
              <Input label="Responsable" value={movement.usuarioResponsable} onChange={(event) => setField('usuarioResponsable', event.target.value)} required />
              <Input label="Observacion" value={movement.observacion} onChange={(event) => setField('observacion', event.target.value)} />
              <Button type="submit"><Send size={16} />Registrar</Button>
            </form>
          ) : (
            <EmptyState title="Accion no permitida" message="El backend puede rechazar movimientos fisicos para tu rol." />
          )}
        </Card>

        <Card>
          <CardHeader title="Consulta de kardex" subtitle="Historial de movimientos para un inventario especifico." meta="Datos reales" />
          <form className="inline-form" onSubmit={buscarKardex}>
            <Input placeholder="Inventario ID" type="number" value={kardexId} onChange={(event) => setKardexId(event.target.value)} required />
            <Button type="submit" disabled={loading}><Search size={16} />Consultar</Button>
          </form>
          <p className="muted-note">Listado general de inventarios pendiente de endpoint backend.</p>
        </Card>
      </div>

      <Card>
        <CardHeader
          title="Kardex consultado"
          subtitle="Movimientos ordenados por fecha segun respuesta del backend."
          meta={`${formatNumber((kardex || []).length)} movimientos`}
        />
        <Table
          rows={kardex || []}
          compact
          columns={[
            { key: 'fechaMovimiento', header: 'Fecha', render: (row) => formatDateTime(row.fechaMovimiento) },
            { key: 'tipoMovimiento', header: 'Tipo', render: (row) => <Badge className="movement-type" tone={movementTone(row.tipoMovimiento)}>{formatMovement(row.tipoMovimiento)}</Badge> },
            { key: 'medicamentoNombre', header: 'Medicamento' },
            { key: 'numeroLote', header: 'Lote', render: (row) => <span className="code-chip">{row.numeroLote}</span> },
            { key: 'cantidad', header: 'Cantidad', align: 'right', render: (row) => formatNumber(row.cantidad) },
            { key: 'stockDespues', header: 'Stock despues', align: 'right', render: (row) => formatNumber(row.stockDespues) },
          ]}
          emptyTitle="Sin kardex cargado"
          emptyMessage="Consulta un inventario para visualizar su kardex."
        />
      </Card>
    </div>
  );
}

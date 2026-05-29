import { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, Boxes, Layers, PackageCheck, Search, Send } from 'lucide-react';
import { inventarioService } from '../api/inventarioService';
import { useAuth } from '../auth/AuthContext';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import EmptyState from '../components/ui/EmptyState';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import Loading from '../components/ui/Loading';
import Select from '../components/ui/Select';
import StatCard from '../components/ui/StatCard';
import Table from '../components/ui/Table';
import { formatDate, formatDateTime, formatNumber } from '../utils/formatters';
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

const negativeMovements = ['SALIDA', 'AJUSTE_NEGATIVO'];

function formatMovement(value) {
  return String(value || '').replaceAll('_', ' ');
}

function movementTone(value) {
  return value?.includes('ENTRADA') || value?.includes('POSITIVO') ? 'success' : 'warning';
}

function stockTone(row) {
  if (row.estadoStock === 'QUIEBRE') return 'danger';
  if (row.estadoStock === 'CRITICO' || row.estadoStock === 'PUNTO_REORDEN') return 'warning';
  return 'success';
}

function lotTone(row) {
  if (row.estado === 'VENCIDO' || row.estado === 'BLOQUEADO') return 'danger';
  if (row.estado === 'AGOTADO' || Number(row.cantidadActual || 0) <= 0) return 'warning';
  return 'success';
}

function inventoryLabel(item) {
  if (!item) return '';
  return `${item.medicamentoCodigo} - ${item.medicamentoNombre} | ${item.centroDistribucionNombre} | Stock: ${formatNumber(item.stockActual)}`;
}

function lotLabel(item) {
  if (!item) return '';
  return `${item.numeroLote} | Vence: ${formatIsoDate(item.fechaVencimiento)} | Cantidad: ${formatNumber(item.cantidadActual)}`;
}

function formatIsoDate(value) {
  if (!value) return 'Sin fecha';
  return String(value).slice(0, 10);
}

export default function Inventario() {
  const { hasAnyRole, user } = useAuth();
  const canRegister = hasAnyRole(INVENTORY_WRITE_ROLES);
  const [inventarios, setInventarios] = useState([]);
  const [lotes, setLotes] = useState([]);
  const [lotesDelMedicamento, setLotesDelMedicamento] = useState([]);
  const [resumen, setResumen] = useState(null);
  const [kardexId, setKardexId] = useState('');
  const [kardex, setKardex] = useState([]);
  const [movement, setMovement] = useState({ ...emptyMovement, usuarioResponsable: user?.username || '' });
  const [loadingInitial, setLoadingInitial] = useState(true);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadData(nextKardexId = kardexId) {
    setError('');
    try {
      const [resumenResult, inventarioResult, lotesResult] = await Promise.allSettled([
        inventarioService.resumenInventario(),
        inventarioService.listarInventarios(),
        inventarioService.listarLotes(),
      ]);

      if (resumenResult.status === 'fulfilled') setResumen(resumenResult.value);
      if (inventarioResult.status === 'fulfilled') setInventarios(inventarioResult.value || []);
      if (lotesResult.status === 'fulfilled') setLotes(lotesResult.value || []);

      const loadErrors = [
        resumenResult.status === 'rejected' ? `Resumen: ${resumenResult.reason?.userMessage || 'no disponible.'}` : '',
        inventarioResult.status === 'rejected' ? `Inventarios: ${inventarioResult.reason?.userMessage || 'no disponibles.'}` : '',
        lotesResult.status === 'rejected' ? `Lotes: ${lotesResult.reason?.userMessage || 'no disponibles.'}` : '',
      ].filter(Boolean);

      if (nextKardexId) {
        const kardexRows = await inventarioService.consultarKardex(nextKardexId);
        setKardex(kardexRows || []);
      }

      if (loadErrors.length) {
        setError(loadErrors.join(' '));
      }
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cargar inventario.');
    } finally {
      setLoadingInitial(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  const selectedInventory = useMemo(
    () => inventarios.find((item) => String(item.id) === String(movement.inventarioId)),
    [inventarios, movement.inventarioId]
  );

  const availableLots = useMemo(() => {
    if (!selectedInventory) return [];
    return lotesDelMedicamento.filter((item) => String(item.medicamentoId) === String(selectedInventory.medicamentoId));
  }, [lotesDelMedicamento, selectedInventory]);

  const selectedLot = useMemo(
    () => availableLots.find((item) => String(item.id) === String(movement.loteMedicamentoId)),
    [availableLots, movement.loteMedicamentoId]
  );

  useEffect(() => {
    let active = true;

    async function loadLotesMedicamento() {
      if (!selectedInventory?.medicamentoId) {
        setLotesDelMedicamento([]);
        return;
      }

      try {
        const rows = await inventarioService.listarLotesPorMedicamento(selectedInventory.medicamentoId);
        if (active) setLotesDelMedicamento(rows || []);
      } catch (exception) {
        if (active) {
          setLotesDelMedicamento([]);
          setError(exception.userMessage || 'No fue posible cargar los lotes del medicamento seleccionado.');
        }
      }
    }

    loadLotesMedicamento();
    return () => {
      active = false;
    };
  }, [selectedInventory?.medicamentoId]);

  function setField(field, value) {
    setMovement((current) => ({
      ...current,
      [field]: value,
      ...(field === 'inventarioId' ? { loteMedicamentoId: '' } : {}),
    }));
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

  function validateMovement(cantidad) {
    if (!selectedInventory) return 'Selecciona un inventario.';
    if (!selectedLot) return 'Selecciona un lote del medicamento.';
    if (!cantidad || cantidad <= 0) return 'La cantidad debe ser mayor a cero.';

    if (negativeMovements.includes(movement.tipoMovimiento)) {
      const stockDisponible = Number(selectedInventory.stockDisponible ?? selectedInventory.stockActual ?? 0);
      const loteDisponible = Number(selectedLot.cantidadActual || 0);
      if (cantidad > stockDisponible) return `La salida supera el stock disponible (${formatNumber(stockDisponible)}).`;
      if (cantidad > loteDisponible) return `La salida supera la cantidad disponible del lote (${formatNumber(loteDisponible)}).`;
    }

    return '';
  }

  async function registrar(event) {
    event.preventDefault();
    setError('');
    setNotice('');

    const cantidad = Number(movement.cantidad);
    const validationMessage = validateMovement(cantidad);
    if (validationMessage) {
      setError(validationMessage);
      return;
    }

    setSaving(true);
    try {
      const payload = {
        ...movement,
        inventarioId: Number(movement.inventarioId),
        loteMedicamentoId: Number(movement.loteMedicamentoId),
        cantidad,
        usuarioResponsable: movement.usuarioResponsable || user?.username || 'frontend',
      };
      const result = await inventarioService.registrarMovimiento(payload);
      setNotice(result.message || 'Movimiento registrado correctamente.');
      const refreshedKardexId = movement.inventarioId;
      setKardexId(refreshedKardexId);
      setMovement({ ...emptyMovement, usuarioResponsable: user?.username || '' });
      await loadData(refreshedKardexId);
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible registrar el movimiento.');
    } finally {
      setSaving(false);
    }
  }

  if (loadingInitial) return <Loading text="Cargando inventario operativo..." />;

  return (
    <div className="page-stack">
      <div className="page-title-row">
        <div>
          <h1>Inventario y movimientos</h1>
          <p>Stock por medicamento, centro y lote con registro controlado de entradas y salidas.</p>
        </div>
      </div>
      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}

      <div className="stat-grid">
        <StatCard label="Inventarios" value={formatNumber(resumen?.totalInventarios ?? inventarios.length)} helper="Medicamento por centro" meta="Total" icon={<Boxes size={22} />} />
        <StatCard label="Stock total" value={formatNumber(resumen?.stockTotal ?? 0)} helper="Unidades registradas" meta="Global" tone="info" icon={<PackageCheck size={22} />} />
        <StatCard label="Disponible" value={formatNumber(resumen?.stockDisponible ?? resumen?.stockDisponibleTotal ?? 0)} helper="Stock no reservado" meta="Operacion" tone="success" icon={<Layers size={22} />} />
        <StatCard label="Riesgos" value={formatNumber(resumen?.riesgosStock ?? 0)} helper="Quiebre, critico o reorden" meta="Alertas" tone="warning" icon={<AlertTriangle size={22} />} />
      </div>

      <div className="two-column-grid inventory-action-grid">
        <Card>
          <CardHeader
            title="Registrar movimiento"
            subtitle={canRegister ? 'Selecciona inventario y lote sin capturar IDs manuales.' : 'No tienes permisos para registrar movimientos.'}
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
              <Select label="Inventario" value={movement.inventarioId} onChange={(event) => setField('inventarioId', event.target.value)} required>
                <option value="">Selecciona inventario</option>
                {inventarios.map((item) => (
                  <option key={item.id} value={item.id}>{inventoryLabel(item)}</option>
                ))}
              </Select>
              <Select label="Lote" value={movement.loteMedicamentoId} onChange={(event) => setField('loteMedicamentoId', event.target.value)} required disabled={!selectedInventory}>
                <option value="">{selectedInventory ? 'Selecciona lote' : 'Selecciona primero un inventario'}</option>
                {availableLots.map((item) => (
                  <option key={item.id} value={item.id} disabled={item.estado === 'VENCIDO' || item.estado === 'BLOQUEADO'}>
                    {lotLabel(item)} | {item.estado}
                  </option>
                ))}
              </Select>
              <Input label="Cantidad" type="number" min="1" value={movement.cantidad} onChange={(event) => setField('cantidad', event.target.value)} required />
              <Input label="Motivo" value={movement.motivo} onChange={(event) => setField('motivo', event.target.value)} required />
              <Input label="Responsable" value={movement.usuarioResponsable} onChange={(event) => setField('usuarioResponsable', event.target.value)} required />
              <Input label="Observacion" value={movement.observacion} onChange={(event) => setField('observacion', event.target.value)} />
              <Button type="submit" disabled={saving}><Send size={16} />{saving ? 'Registrando...' : 'Registrar'}</Button>
            </form>
          ) : (
            <EmptyState title="Accion no permitida" message="Tu rol puede consultar inventario, pero no registrar movimientos fisicos." />
          )}
        </Card>

        <Card>
          <CardHeader title="Consulta de kardex" subtitle="Historial de movimientos por inventario seleccionado." meta="Datos reales" />
          <form className="inline-form" onSubmit={buscarKardex}>
            <Select value={kardexId} onChange={(event) => setKardexId(event.target.value)} required>
              <option value="">Selecciona inventario</option>
              {inventarios.map((item) => (
                <option key={item.id} value={item.id}>{inventoryLabel(item)}</option>
              ))}
            </Select>
            <Button type="submit" disabled={loading}><Search size={16} />Consultar</Button>
          </form>
          <p className="muted-note">Un inventario representa el stock de un medicamento en un centro de distribucion.</p>
        </Card>
      </div>

      <Card>
        <CardHeader title="Inventario total" subtitle="Stock consolidado por medicamento y centro." meta={`${formatNumber(inventarios.length)} inventarios`} />
        <Table
          rows={inventarios}
          compact
          columns={[
            { key: 'medicamentoNombre', header: 'Medicamento', render: (row) => <span><span className="code-chip">{row.medicamentoCodigo}</span> {row.medicamentoNombre}</span> },
            { key: 'centroDistribucionNombre', header: 'Centro' },
            { key: 'stockActual', header: 'Stock', align: 'right', render: (row) => formatNumber(row.stockActual) },
            { key: 'stockDisponible', header: 'Disponible', align: 'right', render: (row) => formatNumber(row.stockDisponible) },
            { key: 'stockReservado', header: 'Reservado', align: 'right', render: (row) => formatNumber(row.stockReservado) },
            { key: 'estadoStock', header: 'Estado', render: (row) => <Badge tone={stockTone(row)}>{formatMovement(row.estadoStock)}</Badge> },
            { key: 'stockMinimo', header: 'Minimo', align: 'right', render: (row) => formatNumber(row.stockMinimo) },
            { key: 'puntoReorden', header: 'Reorden', align: 'right', render: (row) => formatNumber(row.puntoReorden) },
            { key: 'fechaUltimaActualizacion', header: 'Actualizado', render: (row) => formatDateTime(row.fechaUltimaActualizacion) },
          ]}
          emptyTitle="Sin inventario"
          emptyMessage="No hay inventarios registrados para mostrar."
        />
      </Card>

      <Card>
        <CardHeader title="Inventario por lote" subtitle="Disponibilidad y vencimiento por lote de medicamento." meta={`${formatNumber(lotes.length)} lotes`} />
        <Table
          rows={lotes}
          compact
          columns={[
            { key: 'numeroLote', header: 'Lote', render: (row) => <span className="code-chip">{row.numeroLote}</span> },
            { key: 'medicamentoNombre', header: 'Medicamento' },
            { key: 'fechaVencimiento', header: 'Vence', render: (row) => formatDate(row.fechaVencimiento) },
            { key: 'cantidadActual', header: 'Cantidad', align: 'right', render: (row) => formatNumber(row.cantidadActual) },
            { key: 'cantidadInicial', header: 'Inicial', align: 'right', render: (row) => formatNumber(row.cantidadInicial) },
            { key: 'estado', header: 'Estado', render: (row) => <Badge tone={lotTone(row)}>{row.estado}</Badge> },
          ]}
          emptyTitle="Sin lotes"
          emptyMessage="No hay lotes registrados para mostrar."
        />
      </Card>

      <Card>
        <CardHeader title="Kardex consultado" subtitle="Movimientos ordenados por fecha segun respuesta del backend." meta={`${formatNumber((kardex || []).length)} movimientos`} />
        <Table
          rows={kardex || []}
          compact
          columns={[
            { key: 'fechaMovimiento', header: 'Fecha', render: (row) => formatDateTime(row.fechaMovimiento) },
            { key: 'tipoMovimiento', header: 'Tipo', render: (row) => <Badge className="movement-type" tone={movementTone(row.tipoMovimiento)}>{formatMovement(row.tipoMovimiento)}</Badge> },
            { key: 'medicamentoNombre', header: 'Medicamento' },
            { key: 'centroDistribucionNombre', header: 'Centro' },
            { key: 'numeroLote', header: 'Lote', render: (row) => <span className="code-chip">{row.numeroLote}</span> },
            { key: 'cantidad', header: 'Cantidad', align: 'right', render: (row) => formatNumber(row.cantidad) },
            { key: 'stockDespues', header: 'Stock despues', align: 'right', render: (row) => formatNumber(row.stockDespues) },
          ]}
          emptyTitle="Sin kardex cargado"
          emptyMessage="Selecciona un inventario para visualizar su kardex."
        />
      </Card>
    </div>
  );
}

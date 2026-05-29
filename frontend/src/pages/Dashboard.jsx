import { useEffect, useMemo, useState } from 'react';
import { BarChart, Bar, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Activity, AlertTriangle, Boxes, PackageCheck, PackageX, RefreshCcw, ShoppingCart, Truck } from 'lucide-react';
import { dashboardService } from '../api/dashboardService';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import EmptyState from '../components/ui/EmptyState';
import ErrorMessage from '../components/ui/ErrorMessage';
import Loading from '../components/ui/Loading';
import StatCard from '../components/ui/StatCard';
import Table from '../components/ui/Table';
import { formatCurrency, formatDateTime, formatNumber } from '../utils/formatters';

const chartColors = ['#0f766e', '#2563eb', '#65a30d', '#f59e0b', '#dc2626', '#7c3aed'];

function formatStatus(value) {
  return String(value || '').replaceAll('_', ' ');
}

function movementTone(value) {
  return value?.includes('ENTRADA') || value?.includes('POSITIVO') ? 'success' : 'warning';
}

function alertTone(value) {
  if (value === 'QUIEBRE_STOCK') return 'danger';
  if (value === 'STOCK_CRITICO' || value === 'PUNTO_REORDEN') return 'warning';
  return 'info';
}

export default function Dashboard() {
  const [resumen, setResumen] = useState(null);
  const [metricas, setMetricas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadDashboard() {
    setLoading(true);
    setError('');

    try {
      const [resumenData, metricasData] = await Promise.allSettled([
        dashboardService.resumen(),
        dashboardService.metricas(),
      ]);

      if (resumenData.status === 'fulfilled') setResumen(resumenData.value);
      if (metricasData.status === 'fulfilled') setMetricas(metricasData.value || []);

      if (resumenData.status === 'rejected' && metricasData.status === 'rejected') {
        throw resumenData.reason;
      }
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cargar el dashboard.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  const ordenesPorEstado = useMemo(() => {
    const source = resumen?.ordenesPorEstado || {};
    return Object.entries(source).map(([estado, total]) => ({ estado, estadoLabel: formatStatus(estado), total }));
  }, [resumen]);

  const metricasPorTipo = useMemo(() => {
    const grouped = metricas.reduce((acc, item) => {
      const key = item.tipoMetrica || 'SIN_TIPO';
      acc[key] = (acc[key] || 0) + Number(item.valor || 0);
      return acc;
    }, {});
    return Object.entries(grouped).map(([tipo, valor]) => ({ tipo, tipoLabel: formatStatus(tipo), valor }));
  }, [metricas]);

  const stockPorCategoria = useMemo(() => {
    const source = resumen?.stockPorCategoria || {};
    return Object.entries(source).map(([nombre, total]) => ({ nombre, total }));
  }, [resumen]);

  const stockPorCentro = useMemo(() => {
    const source = resumen?.stockPorCentro || {};
    return Object.entries(source).map(([nombre, total]) => ({ nombre, total }));
  }, [resumen]);

  const totalOrdenes = useMemo(
    () => ordenesPorEstado.reduce((sum, item) => sum + Number(item.total || 0), 0),
    [ordenesPorEstado]
  );

  async function handleRecalcular() {
    setNotice('');
    setError('');
    try {
      const result = await dashboardService.recalcular();
      setNotice(result.message || 'Metricas recalculadas correctamente.');
      await loadDashboard();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible recalcular las metricas.');
    }
  }

  if (loading) return <Loading text="Cargando metricas de operacion..." />;

  return (
    <div className="page-stack">
      <div className="page-title-row">
        <div>
          <h1>Dashboard analitico</h1>
          <p>Indicadores operativos de inventario, compras y alertas.</p>
        </div>
        <Button onClick={handleRecalcular}>
          <RefreshCcw size={16} />
          Recalcular
        </Button>
      </div>

      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}

      {!resumen ? (
        <EmptyState title="Dashboard sin datos" message="El backend no retorno resumen disponible." />
      ) : (
        <>
          <div className="stat-grid">
            <StatCard label="Medicamentos activos" value={formatNumber(resumen.totalMedicamentosActivos ?? 0)} helper="Total activo del catalogo" meta="Actualizado" icon={<PackageCheck size={22} />} />
            <StatCard label="Proveedores activos" value={formatNumber(resumen.totalProveedoresActivos ?? 0)} helper="Aliados disponibles" meta="Total activo" tone="info" icon={<Truck size={22} />} />
            <StatCard label="Inventarios" value={formatNumber(resumen.totalInventarios ?? 0)} helper="Centros con stock registrado" meta="Operacion" tone="info" icon={<Boxes size={22} />} />
            <StatCard label="Ordenes pendientes" value={formatNumber(resumen.totalOrdenesPendientes ?? 0)} helper={formatCurrency(resumen.valorTotalOrdenesPendientes)} meta="Por gestionar" tone="warning" icon={<ShoppingCart size={22} />} />
            <StatCard label="Stock critico" value={formatNumber(resumen.totalStockCritico ?? 0)} helper="Inventarios bajo umbral" meta="Riesgo" tone="danger" icon={<AlertTriangle size={22} />} />
            <StatCard label="Alertas pendientes" value={formatNumber(resumen.totalAlertasPendientes ?? 0)} helper="Eventos abiertos" meta="Revision" tone="warning" icon={<Activity size={22} />} />
            <StatCard label="Proximos a agotarse" value={formatNumber((resumen.medicamentosProximosAgotarse || []).length)} helper="Stock sobre minimo y bajo reorden" meta="Preventivo" tone="warning" icon={<PackageCheck size={22} />} />
            <StatCard label="Quiebre de stock" value={formatNumber((resumen.medicamentosEnQuiebre || []).length)} helper="Stock actual igual o menor a cero" meta="Critico" tone="danger" icon={<PackageX size={22} />} />
          </div>

          <div className="dashboard-grid-wide">
            <Card>
              <CardHeader
                title="Ordenes por estado"
                subtitle="Distribucion de compras segun registros reales del backend."
                meta={`${formatNumber(totalOrdenes)} ordenes`}
              />
              {ordenesPorEstado.length ? (
                <>
                  <div className="chart-shell">
                    <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={ordenesPorEstado} margin={{ top: 10, right: 14, left: 0, bottom: 18 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="estadoLabel" tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} />
                    <Tooltip formatter={(value) => [formatNumber(value), 'Ordenes']} labelFormatter={(label) => `Estado: ${label}`} />
                    <Bar dataKey="total" radius={[6, 6, 0, 0]} fill="#0f766e" />
                  </BarChart>
                    </ResponsiveContainer>
                  </div>
                  <div className="chart-summary">
                    {ordenesPorEstado.map((item) => (
                      <Badge key={item.estado} tone={item.estado === 'RECIBIDA' ? 'success' : item.estado === 'CANCELADA' ? 'danger' : 'info'}>
                        {item.estadoLabel}: {formatNumber(item.total)}
                      </Badge>
                    ))}
                  </div>
                </>
              ) : (
                <EmptyState title="Sin ordenes" message="No hay distribucion por estado disponible." />
              )}
            </Card>

            <Card>
              <CardHeader
                title="Metricas por tipo"
                subtitle="Resumen historico calculado por tipo de metrica."
                meta={`${formatNumber(metricasPorTipo.length)} tipos`}
              />
              {metricasPorTipo.length ? (
                <>
                  <div className="chart-shell">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={metricasPorTipo} layout="vertical" margin={{ top: 10, right: 14, left: 18, bottom: 10 }}>
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                        <XAxis type="number" allowDecimals={false} />
                        <YAxis type="category" dataKey="tipoLabel" width={128} tick={{ fontSize: 12 }} />
                        <Tooltip formatter={(value) => [formatNumber(value), 'Valor']} />
                        <Bar dataKey="valor" radius={[0, 6, 6, 0]}>
                          {metricasPorTipo.map((entry, index) => (
                            <Cell key={entry.tipo} fill={chartColors[index % chartColors.length]} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                  <div className="chart-legend">
                    {metricasPorTipo.map((item, index) => (
                      <span className="legend-item" key={item.tipo}>
                        <span className="legend-swatch" style={{ background: chartColors[index % chartColors.length] }} />
                        {item.tipoLabel}
                      </span>
                    ))}
                  </div>
                </>
              ) : (
                <EmptyState title="Sin metricas" message="Ejecuta recalcular cuando el backend tenga datos de inventario." />
              )}
            </Card>
          </div>

          <div className="dashboard-grid-wide">
            <Card>
              <CardHeader title="Stock por categoria" subtitle="Unidades acumuladas por categoria de medicamento." meta={`${formatNumber(stockPorCategoria.length)} categorias`} />
              {stockPorCategoria.length ? (
                <div className="chart-shell chart-shell-sm">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={stockPorCategoria} margin={{ top: 10, right: 14, left: 0, bottom: 28 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis dataKey="nombre" tick={{ fontSize: 12 }} />
                      <YAxis allowDecimals={false} />
                      <Tooltip formatter={(value) => [formatNumber(value), 'Stock']} />
                      <Bar dataKey="total" radius={[6, 6, 0, 0]} fill="#0f766e" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <EmptyState title="Sin stock por categoria" message="No hay inventario agregado por categoria." />
              )}
            </Card>

            <Card>
              <CardHeader title="Stock por centro" subtitle="Disponibilidad consolidada por centro de distribucion." meta={`${formatNumber(stockPorCentro.length)} centros`} />
              {stockPorCentro.length ? (
                <div className="chart-shell chart-shell-sm">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={stockPorCentro} layout="vertical" margin={{ top: 10, right: 14, left: 26, bottom: 10 }}>
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                      <XAxis type="number" allowDecimals={false} />
                      <YAxis type="category" dataKey="nombre" width={132} tick={{ fontSize: 12 }} />
                      <Tooltip formatter={(value) => [formatNumber(value), 'Stock']} />
                      <Bar dataKey="total" radius={[0, 6, 6, 0]} fill="#2563eb" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <EmptyState title="Sin stock por centro" message="No hay inventario agregado por centro." />
              )}
            </Card>
          </div>

          <div className="dashboard-grid-wide">
            <Card>
              <CardHeader title="Medicamentos en quiebre" subtitle="Inventarios con stock actual igual o menor a cero." meta={`${formatNumber((resumen.medicamentosEnQuiebre || []).length)} registros`} />
              <Table
                rows={resumen.medicamentosEnQuiebre || []}
                compact
                columns={[
                  { key: 'medicamentoNombre', header: 'Medicamento', render: (row) => <span><span className="code-chip">{row.medicamentoCodigo}</span> {row.medicamentoNombre}</span> },
                  { key: 'centroDistribucionNombre', header: 'Centro' },
                  { key: 'stockActual', header: 'Stock', align: 'right', render: (row) => formatNumber(row.stockActual) },
                  { key: 'stockMinimo', header: 'Minimo', align: 'right', render: (row) => formatNumber(row.stockMinimo) },
                ]}
                emptyTitle="Sin quiebre"
                emptyMessage="No hay medicamentos en quiebre de stock."
              />
            </Card>

            <Card>
              <CardHeader title="Proximos a agotarse" subtitle="Stock sobre minimo y menor o igual al punto de reorden." meta={`${formatNumber((resumen.medicamentosProximosAgotarse || []).length)} registros`} />
              <Table
                rows={resumen.medicamentosProximosAgotarse || []}
                compact
                columns={[
                  { key: 'medicamentoNombre', header: 'Medicamento', render: (row) => <span><span className="code-chip">{row.medicamentoCodigo}</span> {row.medicamentoNombre}</span> },
                  { key: 'centroDistribucionNombre', header: 'Centro' },
                  { key: 'stockActual', header: 'Stock', align: 'right', render: (row) => formatNumber(row.stockActual) },
                  { key: 'puntoReorden', header: 'Reorden', align: 'right', render: (row) => formatNumber(row.puntoReorden) },
                ]}
                emptyTitle="Sin riesgo preventivo"
                emptyMessage="No hay medicamentos dentro del rango proximo a agotarse."
              />
            </Card>
          </div>

          <div className="dashboard-grid-wide">
            <Card>
              <CardHeader title="Mayor rotacion por salida" subtitle="Medicamentos con mas unidades despachadas." meta={`${formatNumber((resumen.medicamentosMayorRotacion || []).length)} medicamentos`} />
              <Table
                rows={resumen.medicamentosMayorRotacion || []}
                compact
                columns={[
                  { key: 'medicamentoNombre', header: 'Medicamento', render: (row) => <span><span className="code-chip">{row.medicamentoCodigo}</span> {row.medicamentoNombre}</span> },
                  { key: 'totalSalidas', header: 'Salidas', align: 'right', render: (row) => formatNumber(row.totalSalidas) },
                ]}
                emptyTitle="Sin rotacion"
                emptyMessage="No hay salidas registradas para calcular rotacion."
              />
            </Card>

            <Card>
              <CardHeader title="Alertas pendientes" subtitle="Eventos abiertos por quiebre, criticidad o punto de reorden." meta={`${formatNumber((resumen.alertasStock || []).length)} alertas`} />
              <Table
                rows={resumen.alertasStock || []}
                compact
                columns={[
                  { key: 'tipoAlerta', header: 'Tipo', render: (row) => <Badge tone={alertTone(row.tipoAlerta)}>{formatStatus(row.tipoAlerta)}</Badge> },
                  { key: 'medicamentoNombre', header: 'Medicamento' },
                  { key: 'centroDistribucionNombre', header: 'Centro' },
                  { key: 'fechaGeneracion', header: 'Fecha', render: (row) => formatDateTime(row.fechaGeneracion) },
                ]}
                emptyTitle="Sin alertas"
                emptyMessage="No hay alertas pendientes de stock."
              />
            </Card>
          </div>

          <Card>
            <CardHeader
              title="Movimientos recientes"
              subtitle="Ultimas operaciones registradas en inventario."
              meta={`${formatNumber((resumen.movimientosRecientes || []).length)} movimientos`}
            />
            <Table
              rows={resumen.movimientosRecientes || []}
              compact
              columns={[
                { key: 'fechaMovimiento', header: 'Fecha', render: (row) => formatDateTime(row.fechaMovimiento) },
                { key: 'tipoMovimiento', header: 'Tipo', render: (row) => <Badge className="movement-type" tone={movementTone(row.tipoMovimiento)}>{formatStatus(row.tipoMovimiento)}</Badge> },
                { key: 'medicamentoNombre', header: 'Medicamento' },
                { key: 'centroDistribucionNombre', header: 'Centro' },
                { key: 'cantidad', header: 'Cantidad', align: 'right', render: (row) => formatNumber(row.cantidad) },
              ]}
              emptyMessage="No hay movimientos recientes disponibles."
            />
          </Card>
        </>
      )}
    </div>
  );
}

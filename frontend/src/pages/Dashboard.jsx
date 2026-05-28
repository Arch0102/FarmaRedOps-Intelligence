import { useEffect, useMemo, useState } from 'react';
import { BarChart, Bar, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Activity, AlertTriangle, Boxes, PackageCheck, RefreshCcw, ShoppingCart, Truck } from 'lucide-react';
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
    return Object.entries(source).map(([estado, total]) => ({ estado, total }));
  }, [resumen]);

  const metricasPorTipo = useMemo(() => {
    const grouped = metricas.reduce((acc, item) => {
      const key = item.tipoMetrica || 'SIN_TIPO';
      acc[key] = (acc[key] || 0) + Number(item.valor || 0);
      return acc;
    }, {});
    return Object.entries(grouped).map(([tipo, valor]) => ({ tipo, valor }));
  }, [metricas]);

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
            <StatCard label="Medicamentos activos" value={formatNumber(resumen.totalMedicamentosActivos ?? 0)} icon={<PackageCheck size={22} />} />
            <StatCard label="Proveedores activos" value={formatNumber(resumen.totalProveedoresActivos ?? 0)} icon={<Truck size={22} />} />
            <StatCard label="Inventarios" value={formatNumber(resumen.totalInventarios ?? 0)} icon={<Boxes size={22} />} />
            <StatCard label="Ordenes pendientes" value={formatNumber(resumen.totalOrdenesPendientes ?? 0)} helper={formatCurrency(resumen.valorTotalOrdenesPendientes)} icon={<ShoppingCart size={22} />} />
            <StatCard label="Stock critico" value={formatNumber(resumen.totalStockCritico ?? 0)} icon={<AlertTriangle size={22} />} />
            <StatCard label="Alertas pendientes" value={formatNumber(resumen.totalAlertasPendientes ?? 0)} icon={<Activity size={22} />} />
          </div>

          <div className="dashboard-grid">
            <Card>
              <CardHeader title="Ordenes por estado" subtitle="Distribucion de compras" />
              {ordenesPorEstado.length ? (
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={ordenesPorEstado}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="estado" />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar dataKey="total" radius={[6, 6, 0, 0]} fill="#0f766e" />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState title="Sin ordenes" message="No hay distribucion por estado disponible." />
              )}
            </Card>

            <Card>
              <CardHeader title="Metricas por tipo" subtitle="Historico calculado" />
              {metricasPorTipo.length ? (
                <ResponsiveContainer width="100%" height={280}>
                  <PieChart>
                    <Pie data={metricasPorTipo} dataKey="valor" nameKey="tipo" outerRadius={95} label>
                      {metricasPorTipo.map((entry, index) => (
                        <Cell key={entry.tipo} fill={chartColors[index % chartColors.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState title="Sin metricas" message="Ejecuta recalcular cuando el backend tenga datos de inventario." />
              )}
            </Card>
          </div>

          <Card>
            <CardHeader title="Movimientos recientes" subtitle="Ultimas operaciones registradas" />
            <Table
              rows={resumen.movimientosRecientes || []}
              columns={[
                { key: 'fechaMovimiento', header: 'Fecha', render: (row) => formatDateTime(row.fechaMovimiento) },
                { key: 'tipoMovimiento', header: 'Tipo', render: (row) => <Badge tone={row.tipoMovimiento?.includes('ENTRADA') ? 'success' : 'warning'}>{row.tipoMovimiento}</Badge> },
                { key: 'medicamentoNombre', header: 'Medicamento' },
                { key: 'centroDistribucionNombre', header: 'Centro' },
                { key: 'cantidad', header: 'Cantidad' },
              ]}
              emptyMessage="No hay movimientos recientes disponibles."
            />
          </Card>
        </>
      )}
    </div>
  );
}

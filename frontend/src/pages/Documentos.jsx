import { useEffect, useState } from 'react';
import { Download, FilePlus2, FileText, RefreshCcw, Trash2 } from 'lucide-react';
import { documentoService } from '../api/documentoService';
import Button from '../components/ui/Button';
import Card, { CardHeader } from '../components/ui/Card';
import EmptyState from '../components/ui/EmptyState';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import Loading from '../components/ui/Loading';
import Select from '../components/ui/Select';
import Table from '../components/ui/Table';
import Badge from '../components/ui/Badge';
import { formatDateTime } from '../utils/formatters';
import { useAuth } from '../auth/AuthContext';

const tiposDocumento = ['PDF', 'FACTURA', 'ORDEN_COMPRA', 'SOPORTE_INVENTARIO', 'REPORTE', 'OTRO'];

export default function Documentos() {
  const { user } = useAuth();
  const [rows, setRows] = useState([]);
  const [file, setFile] = useState(null);
  const [metadata, setMetadata] = useState({
    tipoDocumento: 'PDF',
    descripcion: '',
    moduloReferencia: '',
    referenciaId: '',
    usuarioCarga: user?.username || '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      setRows(await documentoService.list());
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible cargar documentos.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  function setField(field, value) {
    setMetadata((current) => ({ ...current, [field]: value }));
  }

  async function upload(event) {
    event.preventDefault();
    if (!file) {
      setError('Selecciona un archivo PDF antes de subir.');
      return;
    }

    setSaving(true);
    setError('');
    try {
      const payload = {
        ...metadata,
        referenciaId: metadata.referenciaId ? Number(metadata.referenciaId) : null,
        usuarioCarga: metadata.usuarioCarga || user?.username || 'frontend',
      };
      const result = await documentoService.upload(file, payload);
      setNotice(result.message || 'Documento subido correctamente.');
      setFile(null);
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible subir el documento.');
    } finally {
      setSaving(false);
    }
  }

  async function download(row) {
    setError('');
    try {
      const response = await documentoService.download(row.id);
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.download = row.nombreOriginal || `documento-${row.id}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible descargar el documento.');
    }
  }

  async function remove(row) {
    if (!window.confirm(`Eliminar documento ${row.nombreOriginal}?`)) return;
    try {
      const result = await documentoService.remove(row.id);
      setNotice(result.message || 'Documento eliminado.');
      await load();
    } catch (exception) {
      setError(exception.userMessage || 'No fue posible eliminar el documento.');
    }
  }

  async function generateDashboardReport() {
    setError('');
    setNotice('');
    try {
      const result = await documentoService.generateDashboardReport();
      setNotice(result.message || 'Reporte generado correctamente.');
      await load();
    } catch (exception) {
      setError(exception.status === 404 ? 'Generacion PDF pendiente de endpoint backend.' : exception.userMessage || 'No fue posible generar el PDF.');
    }
  }

  if (loading) return <Loading />;

  return (
    <div className="page-stack">
      <div className="page-title-row">
        <div>
          <h1>Documentos</h1>
          <p>Gestion de PDFs operativos, soportes y reportes.</p>
        </div>
        <Button variant="secondary" onClick={generateDashboardReport}>
          <RefreshCcw size={16} />
          Generar reporte dashboard
        </Button>
      </div>
      <ErrorMessage message={error} />
      {notice && <div className="success-message">{notice}</div>}

      <div className="two-column-grid">
        <Card>
          <CardHeader title="Subir documento" subtitle="Solo archivos PDF segun validacion del backend." />
          <form className="grid-form single" onSubmit={upload}>
            <label className="file-drop">
              <FilePlus2 size={28} />
              <strong>{file ? file.name : 'Seleccionar PDF'}</strong>
              <span>Arrastra desde tu equipo o usa el selector</span>
              <input type="file" accept="application/pdf,.pdf" onChange={(event) => setFile(event.target.files?.[0] || null)} />
            </label>
            <Select label="Tipo" value={metadata.tipoDocumento} onChange={(event) => setField('tipoDocumento', event.target.value)}>
              {tiposDocumento.map((tipo) => <option key={tipo} value={tipo}>{tipo}</option>)}
            </Select>
            <Input label="Descripcion" value={metadata.descripcion} onChange={(event) => setField('descripcion', event.target.value)} />
            <Input label="Modulo referencia" value={metadata.moduloReferencia} onChange={(event) => setField('moduloReferencia', event.target.value)} />
            <Input label="Referencia ID" type="number" value={metadata.referenciaId} onChange={(event) => setField('referenciaId', event.target.value)} />
            <Input label="Usuario carga" value={metadata.usuarioCarga} onChange={(event) => setField('usuarioCarga', event.target.value)} required />
            <Button type="submit" disabled={saving}>{saving ? 'Subiendo...' : 'Subir documento'}</Button>
          </form>
        </Card>

        <Card>
          <CardHeader title="Estado del modulo" />
          <EmptyState
            title="Descarga binaria preservada"
            message="La descarga usa responseType blob porque el backend no envuelve archivos en ApiResponse."
          />
        </Card>
      </div>

      <Card>
        <CardHeader title="Documentos activos" />
        <Table
          rows={rows}
          columns={[
            { key: 'nombreOriginal', header: 'Archivo', render: (row) => <span className="file-name"><FileText size={16} />{row.nombreOriginal}</span> },
            { key: 'tipoDocumento', header: 'Tipo', render: (row) => <Badge tone="info">{row.tipoDocumento}</Badge> },
            { key: 'estadoDocumento', header: 'Estado', render: (row) => <Badge tone={row.activo ? 'success' : 'neutral'}>{row.estadoDocumento}</Badge> },
            { key: 'usuarioCarga', header: 'Usuario' },
            { key: 'fechaCarga', header: 'Fecha', render: (row) => formatDateTime(row.fechaCarga) },
            { key: 'actions', header: 'Acciones', render: (row) => <div className="row-actions"><Button variant="ghost" size="sm" onClick={() => download(row)}><Download size={15} />Descargar</Button><Button variant="danger" size="sm" onClick={() => remove(row)}><Trash2 size={15} />Eliminar</Button></div> },
          ]}
          emptyMessage="No hay documentos activos."
        />
      </Card>
    </div>
  );
}

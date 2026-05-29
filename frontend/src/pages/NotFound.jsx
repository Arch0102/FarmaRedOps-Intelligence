import { Link } from 'react-router-dom';
import Card from '../components/ui/Card';

export default function NotFound() {
  return (
    <Card className="center-card">
      <h1>Pagina no encontrada</h1>
      <p>La ruta solicitada no existe dentro de FarmaRed Ops-Intelligence.</p>
      <Link className="btn btn-primary btn-md" to="/dashboard">
        Ir al inicio
      </Link>
    </Card>
  );
}

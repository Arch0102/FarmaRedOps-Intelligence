import { ShieldAlert } from 'lucide-react';
import { Link } from 'react-router-dom';
import Card from '../components/ui/Card';

export default function Forbidden() {
  return (
    <Card className="center-card">
      <ShieldAlert size={44} />
      <h1>No tienes permisos para esta accion</h1>
      <p>La seguridad del backend valida cada operacion. Si necesitas acceso, solicita un rol autorizado.</p>
      <Link className="btn btn-primary btn-md" to="/dashboard">
        Volver al dashboard
      </Link>
    </Card>
  );
}

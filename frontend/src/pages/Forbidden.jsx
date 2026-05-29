import { ShieldAlert } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import Card from '../components/ui/Card';
import { getDefaultRouteForRoles, hasOperationalRole } from '../utils/roles';

export default function Forbidden() {
  const { roles } = useAuth();
  const hasAccessProfile = hasOperationalRole(roles);
  const target = getDefaultRouteForRoles(roles);

  return (
    <Card className="center-card">
      <ShieldAlert size={44} />
      <h1>No tienes permisos para esta accion</h1>
      <p>
        {hasAccessProfile
          ? 'La seguridad del backend valida cada operacion. Si necesitas acceso, solicita un rol autorizado.'
          : 'Tu usuario no tiene un rol operativo valido. Solicita a un ADMIN_AUDITOR asignar Auxiliar de bodega, Analista de compras o Admin auditor.'}
      </p>
      {hasAccessProfile && (
        <Link className="btn btn-primary btn-md" to={target}>
          Volver al inicio
        </Link>
      )}
    </Card>
  );
}

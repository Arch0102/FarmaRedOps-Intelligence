import Card, { CardHeader } from '../components/ui/Card';
import EmptyState from '../components/ui/EmptyState';

export default function Usuarios() {
  return (
    <div className="page-stack">
      <div className="page-title-row">
        <div>
          <h1>Usuarios y roles</h1>
          <p>Modulo administrativo reservado para ADMIN_AUDITOR.</p>
        </div>
      </div>
      <Card>
        <CardHeader title="Gestion de usuarios" subtitle="No se encontraron endpoints de usuario en controllers actuales." />
        <EmptyState title="Modulo preparado para integracion backend" message="Cuando existan endpoints de usuarios, esta vista puede conectarse sin cambiar la navegacion." />
      </Card>
    </div>
  );
}

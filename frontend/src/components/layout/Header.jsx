import { Menu, ShieldCheck } from 'lucide-react';
import { useAuth } from '../../auth/AuthContext';
import { roleLabel } from '../../utils/roles';
import Button from '../ui/Button';
import Badge from '../ui/Badge';

export default function Header({ onMenuClick }) {
  const { user, roles } = useAuth();
  const mainRole = roles[0];

  return (
    <header className="app-header">
      <div className="header-left">
        <Button className="menu-button" variant="ghost" size="icon" onClick={onMenuClick} aria-label="Abrir menu">
          <Menu size={20} />
        </Button>
        <div>
          <h1>FarmaRed Ops-Intelligence</h1>
          <p>Gestion operativa y analitica farmaceutica</p>
        </div>
      </div>

      <div className="header-user">
        <div className="user-avatar">{(user?.username || 'U').slice(0, 1).toUpperCase()}</div>
        <div>
          <strong>{user?.username || 'Usuario'}</strong>
          <span>{user?.email || 'Sesion activa'}</span>
        </div>
        <Badge tone="info">
          <ShieldCheck size={14} />
          {roleLabel(mainRole)}
        </Badge>
      </div>
    </header>
  );
}

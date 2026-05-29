import {
  BarChart3,
  Boxes,
  Building2,
  FileText,
  LayoutDashboard,
  LogOut,
  Package,
  ShoppingCart,
  Tags,
  Truck,
  Users,
  X,
} from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { ADMIN_ROLES, CATALOG_READ_ROLES, DASHBOARD_ROLES, INVENTORY_WRITE_ROLES, PURCHASE_ROLES } from '../../utils/roles';
import Button from '../ui/Button';

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, roles: DASHBOARD_ROLES },
  { to: '/medicamentos', label: 'Medicamentos', icon: Package, roles: CATALOG_READ_ROLES },
  { to: '/categorias', label: 'Categorias', icon: Tags, roles: CATALOG_READ_ROLES },
  { to: '/centros', label: 'Centros', icon: Building2, roles: CATALOG_READ_ROLES },
  { to: '/inventario', label: 'Inventario', icon: Boxes, roles: [...INVENTORY_WRITE_ROLES, ...DASHBOARD_ROLES] },
  { to: '/proveedores', label: 'Proveedores', icon: Truck, roles: PURCHASE_ROLES },
  { to: '/ordenes-compra', label: 'Ordenes', icon: ShoppingCart, roles: PURCHASE_ROLES },
  { to: '/documentos', label: 'Documentos', icon: FileText, roles: CATALOG_READ_ROLES },
  { to: '/usuarios', label: 'Usuarios', icon: Users, roles: ADMIN_ROLES },
];

export default function Sidebar({ open, onClose }) {
  const { hasAnyRole, logout } = useAuth();
  const visibleItems = navItems.filter((item) => hasAnyRole(item.roles));

  return (
    <>
      <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
        <div className="sidebar-brand">
          <div className="brand-mark">
            <BarChart3 size={20} />
          </div>
          <div>
            <strong>FarmaRed</strong>
            <span>Ops-Intelligence</span>
          </div>
          <Button className="sidebar-close" variant="ghost" size="icon" onClick={onClose} aria-label="Cerrar menu">
            <X size={18} />
          </Button>
        </div>

        <nav className="sidebar-nav">
          {visibleItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink key={item.to} to={item.to} onClick={onClose} className={({ isActive }) => (isActive ? 'active' : '')}>
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        <div className="sidebar-footer">
          <Button variant="secondary" className="logout-button" onClick={logout}>
            <LogOut size={16} />
            Cerrar sesion
          </Button>
        </div>
      </aside>
      {open && <button className="sidebar-overlay" aria-label="Cerrar menu" onClick={onClose} />}
    </>
  );
}

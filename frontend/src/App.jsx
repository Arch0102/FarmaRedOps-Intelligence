import { Navigate, Route, Routes } from 'react-router-dom';
import RoleGuard from './auth/RoleGuard';
import ProtectedRoute from './auth/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';
import Categorias from './pages/Categorias';
import CentrosDistribucion from './pages/CentrosDistribucion';
import Dashboard from './pages/Dashboard';
import Documentos from './pages/Documentos';
import Forbidden from './pages/Forbidden';
import Inventario from './pages/Inventario';
import Login from './pages/Login';
import Medicamentos from './pages/Medicamentos';
import NotFound from './pages/NotFound';
import OrdenesCompra from './pages/OrdenesCompra';
import Proveedores from './pages/Proveedores';
import Register from './pages/Register';
import Usuarios from './pages/Usuarios';
import {
  ADMIN_ROLES,
  CATALOG_READ_ROLES,
  DASHBOARD_ROLES,
  INVENTORY_WRITE_ROLES,
  PURCHASE_ROLES,
} from './utils/roles';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="/forbidden" element={<Forbidden />} />
          <Route
            path="/dashboard"
            element={<RoleGuard roles={DASHBOARD_ROLES}><Dashboard /></RoleGuard>}
          />
          <Route
            path="/medicamentos"
            element={<RoleGuard roles={CATALOG_READ_ROLES}><Medicamentos /></RoleGuard>}
          />
          <Route
            path="/categorias"
            element={<RoleGuard roles={CATALOG_READ_ROLES}><Categorias /></RoleGuard>}
          />
          <Route
            path="/centros"
            element={<RoleGuard roles={CATALOG_READ_ROLES}><CentrosDistribucion /></RoleGuard>}
          />
          <Route
            path="/inventario"
            element={<RoleGuard roles={[...INVENTORY_WRITE_ROLES, ...DASHBOARD_ROLES]}><Inventario /></RoleGuard>}
          />
          <Route
            path="/proveedores"
            element={<RoleGuard roles={PURCHASE_ROLES}><Proveedores /></RoleGuard>}
          />
          <Route
            path="/ordenes-compra"
            element={<RoleGuard roles={PURCHASE_ROLES}><OrdenesCompra /></RoleGuard>}
          />
          <Route
            path="/documentos"
            element={<RoleGuard roles={CATALOG_READ_ROLES}><Documentos /></RoleGuard>}
          />
          <Route
            path="/usuarios"
            element={<RoleGuard roles={ADMIN_ROLES}><Usuarios /></RoleGuard>}
          />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

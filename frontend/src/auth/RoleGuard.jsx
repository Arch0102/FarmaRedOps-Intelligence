import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

export default function RoleGuard({ roles, children, fallback = null }) {
  const { hasAnyRole } = useAuth();

  if (!roles || roles.length === 0 || hasAnyRole(roles)) {
    return children;
  }

  if (fallback) return fallback;

  return <Navigate to="/forbidden" replace />;
}

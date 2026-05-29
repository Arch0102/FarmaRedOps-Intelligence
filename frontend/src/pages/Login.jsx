import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { LogIn } from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import Button from '../components/ui/Button';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import { getDefaultRouteForRoles } from '../utils/roles';

export default function Login() {
  const { login, isAuthenticated, roles } = useAuth();
  const [form, setForm] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  if (isAuthenticated) {
    return <Navigate to={getDefaultRouteForRoles(roles)} replace />;
  }

  const from = location.state?.from?.pathname;

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const result = await login(form);
      navigate(from || getDefaultRouteForRoles(result.user?.roles), { replace: true });
    } catch (exception) {
      setError(exception.userMessage || exception.message || 'No fue posible iniciar sesion.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <div className="auth-brand">
          <div className="brand-mark large">
            <LogIn size={26} />
          </div>
          <div>
            <h1>FarmaRed Ops-Intelligence</h1>
            <p>Ingresa para operar inventario, compras y documentos.</p>
          </div>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <Input
            label="Usuario"
            value={form.username}
            onChange={(event) => setForm((current) => ({ ...current, username: event.target.value }))}
            required
            autoComplete="username"
          />
          <Input
            label="Contrasena"
            type="password"
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            required
            autoComplete="current-password"
          />
          <ErrorMessage message={error} />
          <Button type="submit" disabled={loading}>
            {loading ? 'Validando...' : 'Iniciar sesion'}
          </Button>
        </form>

        <p className="auth-switch">
          No tienes cuenta? <Link to="/register">Crear usuario</Link>
        </p>
      </section>
    </main>
  );
}

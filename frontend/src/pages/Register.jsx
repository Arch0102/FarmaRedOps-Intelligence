import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { UserPlus } from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import Button from '../components/ui/Button';
import ErrorMessage from '../components/ui/ErrorMessage';
import Input from '../components/ui/Input';
import { getDefaultRouteForRoles } from '../utils/roles';

export default function Register() {
  const { register, isAuthenticated, roles } = useAuth();
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    nombreCompleto: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  if (isAuthenticated) {
    return <Navigate to={getDefaultRouteForRoles(roles)} replace />;
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const result = await register(form);
      navigate(getDefaultRouteForRoles(result.user?.roles), { replace: true });
    } catch (exception) {
      setError(exception.userMessage || exception.message || 'No fue posible registrar el usuario.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel wide">
        <div className="auth-brand">
          <div className="brand-mark large">
            <UserPlus size={26} />
          </div>
          <div>
            <h1>Registro de usuario</h1>
            <p>El backend asigna el rol operativo inicial definido por seguridad.</p>
          </div>
        </div>

        <form className="auth-form grid-form" onSubmit={handleSubmit}>
          <Input label="Usuario" value={form.username} onChange={(event) => updateField('username', event.target.value)} required />
          <Input label="Email" type="email" value={form.email} onChange={(event) => updateField('email', event.target.value)} required />
          <Input label="Nombre completo" value={form.nombreCompleto} onChange={(event) => updateField('nombreCompleto', event.target.value)} required />
          <Input label="Contrasena" type="password" value={form.password} onChange={(event) => updateField('password', event.target.value)} minLength={6} required />
          <ErrorMessage message={error} />
          <Button type="submit" disabled={loading}>
            {loading ? 'Creando usuario...' : 'Crear usuario'}
          </Button>
        </form>

        <p className="auth-switch">
          Ya tienes cuenta? <Link to="/login">Iniciar sesion</Link>
        </p>
      </section>
    </main>
  );
}

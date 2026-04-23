import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../services/api';
import Alert from '../components/ui/Alert';
import styles from './Login.module.css';

export default function RegisterPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ nombre: '', apellido: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  if (isAuthenticated) return <Navigate to="/" replace />;

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await authApi.register(form);
      setSuccess('Cuenta creada. Redirigiendo al login…');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al registrar. Intente de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <form className={styles.card} onSubmit={handleSubmit}>
        <h1 className={styles.title}>📚 Crear Cuenta</h1>

        <Alert type="error" message={error} />
        <Alert type="success" message={success} />

        {['nombre', 'apellido', 'email', 'password'].map((field) => (
          <label key={field} className={styles.label}>
            {field.charAt(0).toUpperCase() + field.slice(1)}
            <input
              className={styles.input}
              type={field === 'password' ? 'password' : field === 'email' ? 'email' : 'text'}
              name={field}
              value={form[field]}
              onChange={handleChange}
              required
            />
          </label>
        ))}

        <button className={styles.btn} type="submit" disabled={loading}>
          {loading ? 'Registrando…' : 'Registrarse'}
        </button>

        <p className={styles.hint}>
          ¿Ya tienes cuenta? <a href="/login">Ingresa aquí</a>
        </p>
      </form>
    </div>
  );
}

import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/ui/Alert';
import styles from './Login.module.css';

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (isAuthenticated) return <Navigate to="/" replace />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Credenciales inválidas. Intente de nuevo.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <form className={styles.card} onSubmit={handleSubmit}>
        <h1 className={styles.title}>📚 Estructuras de Datos</h1>
        <p className={styles.subtitle}>Ingresa a tu cuenta</p>

        <Alert type="error" message={error} />

        <label className={styles.label}>
          Email
          <input
            className={styles.input}
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="correo@ejemplo.com"
          />
        </label>

        <label className={styles.label}>
          Contraseña
          <input
            className={styles.input}
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="••••••••"
          />
        </label>

        <button className={styles.btn} type="submit" disabled={loading}>
          {loading ? 'Ingresando…' : 'Ingresar'}
        </button>

        <p className={styles.hint}>
          ¿No tienes cuenta?{' '}
          <a href="/register">Regístrate aquí</a>
        </p>
      </form>
    </div>
  );
}

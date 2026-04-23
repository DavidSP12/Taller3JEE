import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import styles from './Navbar.module.css';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className={styles.nav}>
      <div className={styles.brand}>
        <Link to="/">📚 Estructuras de Datos</Link>
      </div>
      <div className={styles.links}>
        {user && (
          <>
            <Link to="/">Cursos</Link>
            {user.inscripcionId && (
              <>
                <Link to={`/inscripciones/${user.inscripcionId}/progreso`}>Mi Progreso</Link>
                <Link to={`/inscripciones/${user.inscripcionId}/recomendaciones`}>Recomendaciones</Link>
              </>
            )}
            {isAdmin && <Link to="/admin">Admin</Link>}
            <span className={styles.email}>{user.email}</span>
            <button onClick={handleLogout} className={styles.logoutBtn}>Salir</button>
          </>
        )}
      </div>
    </nav>
  );
}

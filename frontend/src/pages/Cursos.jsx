import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { cursosApi, inscripcionApi } from '../services/api';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import styles from './Cursos.module.css';

export default function CursosPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [cursos, setCursos] = useState([]);
  const [inscripciones, setInscripciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [enrolling, setEnrolling] = useState(null);

  useEffect(() => {
    Promise.all([
      cursosApi.getCursos(),
      inscripcionApi.getByEstudiante(user.estudianteId),
    ])
      .then(([cursosRes, insRes]) => {
        setCursos(cursosRes.data);
        setInscripciones(insRes.data);
      })
      .catch((err) => setError(err.response?.data?.message || 'Error al cargar cursos'))
      .finally(() => setLoading(false));
  }, [user.estudianteId]);

  const getInscripcion = (cursoId) =>
    inscripciones.find((i) => i.cursoId === cursoId);

  const handleEnroll = async (cursoId) => {
    setEnrolling(cursoId);
    try {
      const { data } = await inscripcionApi.inscribir(user.estudianteId, cursoId);
      setInscripciones((prev) => [...prev.filter((i) => i.cursoId !== cursoId), data]);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al inscribirse');
    } finally {
      setEnrolling(null);
    }
  };

  const handleGo = (cursoId, inscripcionId) => {
    navigate(`/cursos/${cursoId}/clases?inscripcionId=${inscripcionId}`);
  };

  if (loading) return <Spinner message="Cargando cursos…" />;

  return (
    <div>
      <h1 className={styles.title}>Cursos Disponibles</h1>
      <Alert type="error" message={error} />
      {cursos.length === 0 && (
        <p className={styles.empty}>
          No hay cursos disponibles. El administrador debe cargar el curso vía batch.
        </p>
      )}
      <div className={styles.grid}>
        {cursos.map((curso) => {
          const insc = getInscripcion(curso.id);
          return (
            <div key={curso.id} className={styles.card}>
              <div className={styles.cardHeader}>
                <span className={`${styles.badge} ${styles[curso.estado?.toLowerCase()]}`}>
                  {curso.estado}
                </span>
              </div>
              <h2 className={styles.cardTitle}>{curso.nombre}</h2>
              <p className={styles.cardDesc}>{curso.descripcion}</p>
              <p className={styles.cardMeta}>Versión {curso.version}</p>
              <div className={styles.cardActions}>
                {insc ? (
                  <button
                    className={styles.btnPrimary}
                    onClick={() => handleGo(curso.id, insc.id)}
                  >
                    Continuar →
                  </button>
                ) : (
                  <button
                    className={styles.btnEnroll}
                    onClick={() => handleEnroll(curso.id)}
                    disabled={enrolling === curso.id}
                  >
                    {enrolling === curso.id ? 'Inscribiendo…' : 'Inscribirse'}
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

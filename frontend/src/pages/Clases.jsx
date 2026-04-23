import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { cursosApi } from '../services/api';
import { useFetch } from '../hooks/useFetch';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import styles from './Clases.module.css';

const ESTADO_LABELS = { NO_INICIADO: '○', EN_PROGRESO: '◑', COMPLETADO: '✓' };
const ESTADO_CLASS  = { NO_INICIADO: styles.noIniciado, EN_PROGRESO: styles.enProgreso, COMPLETADO: styles.completado };

export default function ClasesPage() {
  const { cursoId } = useParams();
  const [searchParams] = useSearchParams();
  const inscripcionId = searchParams.get('inscripcionId');
  const navigate = useNavigate();

  const { data: curso, loading: loadingCurso, error: errorCurso } =
    useFetch(() => cursosApi.getCurso(cursoId), [cursoId]);

  const { data: clases, loading: loadingClases, error: errorClases } =
    useFetch(() => cursosApi.getClasesByCurso(cursoId), [cursoId]);

  if (loadingCurso || loadingClases) return <Spinner message="Cargando clases…" />;
  if (errorCurso || errorClases) return <Alert type="error" message={errorCurso || errorClases} />;

  return (
    <div>
      <button className={styles.back} onClick={() => navigate('/')}>← Volver</button>
      <h1 className={styles.title}>{curso?.nombre}</h1>
      <p className={styles.desc}>{curso?.descripcion}</p>

      <div className={styles.list}>
        {(clases || []).map((clase) => (
          <div key={clase.id} className={styles.card}
            onClick={() => navigate(`/clases/${clase.id}?inscripcionId=${inscripcionId}`)}>
            <div className={styles.numero}>#{clase.numero}</div>
            <div className={styles.info}>
              <h3 className={styles.claseTitulo}>{clase.titulo}</h3>
              <p className={styles.claseDesc}>{clase.descripcion}</p>
              <span className={styles.duracion}>⏱ {clase.duracionEstimadaMin} min</span>
            </div>
            <span className={styles.arrow}>›</span>
          </div>
        ))}
      </div>
    </div>
  );
}

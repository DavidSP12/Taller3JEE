import { useParams } from 'react-router-dom';
import { recomendacionesApi } from '../services/api';
import { useFetch } from '../hooks/useFetch';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import styles from './Recomendaciones.module.css';

const TIPO_ICON = {
  REFUERZO:       '🔁',
  SIGUIENTE_TEMA: '▶',
  RECORDATORIO:   '🔔',
};

const PRIORIDAD_CLS = { ALTA: 'alta', MEDIA: 'media', BAJA: 'baja' };

export default function RecomendacionesPage() {
  const { inscripcionId } = useParams();

  const { data: recs, loading, error, setData } = useFetch(
    () => recomendacionesApi.getRecomendaciones(inscripcionId), [inscripcionId]
  );

  const handleEstado = async (id, estado) => {
    try {
      await recomendacionesApi.actualizarEstado(id, estado);
      setData((prev) => prev.filter((r) => r.id !== id));
    } catch (_) { /* ignore */ }
  };

  if (loading) return <Spinner message="Cargando recomendaciones…" />;
  if (error)   return <Alert type="error" message={error} />;

  return (
    <div>
      <h1 className={styles.title}>Mis Recomendaciones</h1>
      {(!recs || recs.length === 0) && (
        <div className={styles.empty}>
          <p>🎯 No tienes recomendaciones pendientes.</p>
          <p className={styles.hint}>El sistema generará recomendaciones a medida que avances.</p>
        </div>
      )}
      <div className={styles.list}>
        {(recs || []).map((r) => (
          <div key={r.id} className={styles.card}>
            <div className={styles.cardLeft}>
              <span className={styles.tipoIcon}>{TIPO_ICON[r.tipo] ?? '📌'}</span>
            </div>
            <div className={styles.cardBody}>
              <div className={styles.cardTop}>
                <span className={styles.tipo}>{r.tipo.replace('_', ' ')}</span>
                <span className={`${styles.prioridad} ${styles[PRIORIDAD_CLS[r.prioridad] ?? 'baja']}`}>
                  {r.prioridad}
                </span>
              </div>
              {r.claseTitulo && (
                <p className={styles.clase}>📚 {r.claseTitulo}</p>
              )}
              <p className={styles.motivo}>{r.motivo}</p>
              <p className={styles.fecha}>
                {r.fechaGenerada ? new Date(r.fechaGenerada).toLocaleDateString('es-CO') : ''}
              </p>
            </div>
            <div className={styles.cardActions}>
              <button
                className={styles.btnVisto}
                onClick={() => handleEstado(r.id, 'VISTA')}
                title="Marcar como vista"
              >
                ✓
              </button>
              <button
                className={styles.btnDescartar}
                onClick={() => handleEstado(r.id, 'DESCARTADA')}
                title="Descartar"
              >
                ✕
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

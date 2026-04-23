import { useParams } from 'react-router-dom';
import { inscripcionApi } from '../services/api';
import { useFetch } from '../hooks/useFetch';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import styles from './Progreso.module.css';

const ESTADO_CONFIG = {
  COMPLETADO:  { label: 'Completado',  cls: 'completado',  icon: '✓' },
  EN_PROGRESO: { label: 'En progreso', cls: 'enProgreso',  icon: '◑' },
  NO_INICIADO: { label: 'No iniciado', cls: 'noIniciado',  icon: '○' },
};

export default function ProgresoPage() {
  const { inscripcionId } = useParams();

  const { data: progresos, loading, error } = useFetch(
    () => inscripcionApi.getProgreso(inscripcionId), [inscripcionId]
  );

  const completadas = progresos?.filter((p) => p.estado === 'COMPLETADO').length ?? 0;
  const total = progresos?.length ?? 0;
  const pct = total > 0 ? Math.round((completadas / total) * 100) : 0;

  if (loading) return <Spinner message="Cargando progreso…" />;
  if (error)   return <Alert type="error" message={error} />;

  return (
    <div>
      <h1 className={styles.title}>Mi Progreso</h1>

      <div className={styles.summary}>
        <div className={styles.summaryItem}>
          <span className={styles.big}>{completadas}</span>
          <span className={styles.label}>Completadas</span>
        </div>
        <div className={styles.summaryItem}>
          <span className={styles.big}>{total}</span>
          <span className={styles.label}>Total clases</span>
        </div>
        <div className={styles.summaryItem}>
          <span className={styles.big}>{pct}%</span>
          <span className={styles.label}>Completado</span>
        </div>
      </div>

      <div className={styles.bar}>
        <div className={styles.fill} style={{ width: `${pct}%` }} />
      </div>

      <div className={styles.list}>
        {(progresos || []).map((p) => {
          const cfg = ESTADO_CONFIG[p.estado] ?? ESTADO_CONFIG.NO_INICIADO;
          return (
            <div key={p.id} className={styles.row}>
              <span className={`${styles.icon} ${styles[cfg.cls]}`}>{cfg.icon}</span>
              <div className={styles.info}>
                <span className={styles.claseTitulo}>{p.claseTitulo}</span>
                {p.tiempoTotalSegundos > 0 && (
                  <span className={styles.tiempo}>
                    ⏱ {Math.round(p.tiempoTotalSegundos / 60)} min
                  </span>
                )}
              </div>
              <span className={`${styles.estadoBadge} ${styles[cfg.cls]}`}>{cfg.label}</span>
            </div>
          );
        })}
        {progresos?.length === 0 && (
          <p className={styles.empty}>Aún no has iniciado ninguna clase.</p>
        )}
      </div>
    </div>
  );
}

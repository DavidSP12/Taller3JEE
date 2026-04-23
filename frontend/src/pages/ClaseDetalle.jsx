import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { clasesApi, inscripcionApi } from '../services/api';
import { useFetch } from '../hooks/useFetch';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import styles from './ClaseDetalle.module.css';

const TIPO_ICON = {
  TEXT: '📄', PDF: '📑', VIDEO: '🎬', PPTX: '📊',
  IMAGE: '🖼', URL: '🔗', WORD: '📝', EXCEL: '📈',
};

export default function ClaseDetallePage() {
  const { claseId } = useParams();
  const [searchParams] = useSearchParams();
  const inscripcionId = searchParams.get('inscripcionId');
  const navigate = useNavigate();

  const [feedback, setFeedback] = useState('');
  const [iniciando, setIniciando] = useState(false);
  const [completando, setCompletando] = useState(false);
  const [progresoState, setProgresoState] = useState(null);

  const { data: clase, loading: lClase } = useFetch(() => clasesApi.getClase(claseId), [claseId]);
  const { data: contenidos, loading: lContenidos } = useFetch(
    () => clasesApi.getContenidos(claseId), [claseId]
  );
  const { data: evaluacion } = useFetch(
    () => clasesApi.getEvaluacion(claseId).catch(() => ({ data: null })),
    [claseId]
  );

  const handleIniciar = async () => {
    if (!inscripcionId) return setFeedback('No tienes inscripción activa.');
    setIniciando(true);
    try {
      const { data } = await inscripcionApi.iniciarClase(inscripcionId, claseId);
      setProgresoState(data.estado);
      setFeedback('Clase iniciada. ¡Comienza a revisar el contenido!');
    } catch (e) {
      setFeedback(e.response?.data?.message || 'Error al iniciar clase.');
    } finally {
      setIniciando(false);
    }
  };

  const handleInteraccion = async (contenidoId) => {
    if (!inscripcionId) return;
    try {
      await inscripcionApi.registrarInteraccion(inscripcionId, contenidoId, {
        duracionSegundos: 120,
        completado: true,
      });
    } catch (_) { /* silently ignore */ }
  };

  const handleCompletar = async () => {
    if (!inscripcionId) return setFeedback('No tienes inscripción activa.');
    setCompletando(true);
    try {
      const { data } = await inscripcionApi.completarClase(inscripcionId, claseId);
      setProgresoState(data.estado);
      setFeedback('¡Clase completada!');
    } catch (e) {
      setFeedback(e.response?.data?.message || 'Error al completar clase.');
    } finally {
      setCompletando(false);
    }
  };

  if (lClase || lContenidos) return <Spinner message="Cargando clase…" />;

  return (
    <div>
      <button className={styles.back} onClick={() => navigate(-1)}>← Volver</button>

      <div className={styles.header}>
        <div>
          <span className={styles.numero}>Clase {clase?.numero}</span>
          <h1 className={styles.title}>{clase?.titulo}</h1>
          <p className={styles.desc}>{clase?.descripcion}</p>
        </div>
        <div className={styles.actions}>
          {progresoState !== 'EN_PROGRESO' && progresoState !== 'COMPLETADO' && (
            <button className={styles.btnStart} onClick={handleIniciar} disabled={iniciando}>
              {iniciando ? 'Iniciando…' : '▶ Iniciar Clase'}
            </button>
          )}
          {progresoState === 'EN_PROGRESO' && (
            <button className={styles.btnComplete} onClick={handleCompletar} disabled={completando}>
              {completando ? 'Completando…' : '✓ Marcar como completada'}
            </button>
          )}
        </div>
      </div>

      {feedback && <Alert type="info" message={feedback} />}

      <h2 className={styles.section}>Contenidos</h2>
      <div className={styles.contenidoList}>
        {(contenidos || []).map((c) => (
          <div key={c.id} className={styles.contenidoCard}
            onClick={() => handleInteraccion(c.id)}>
            <span className={styles.tipoIcon}>{TIPO_ICON[c.tipo] || '📁'}</span>
            <div className={styles.contenidoInfo}>
              <span className={styles.contenidoTitulo}>{c.titulo}</span>
              <span className={styles.contenidoTipo}>{c.tipo}</span>
            </div>
            {c.urlRecurso && (
              <a href={c.urlRecurso} target="_blank" rel="noreferrer"
                className={styles.recursoLink} onClick={(e) => e.stopPropagation()}>
                Abrir ↗
              </a>
            )}
            {c.textoCuerpo && (
              <details className={styles.textDetail}>
                <summary>Leer</summary>
                <p>{c.textoCuerpo}</p>
              </details>
            )}
          </div>
        ))}
      </div>

      {evaluacion && (
        <div className={styles.evalBox}>
          <h2 className={styles.section}>Evaluación</h2>
          <p className={styles.evalTitle}>{evaluacion.titulo}</p>
          <p className={styles.evalMeta}>{evaluacion.preguntas?.length} preguntas · {evaluacion.puntajeMaximo} pts</p>
          <button
            className={styles.btnEval}
            onClick={() =>
              navigate(`/evaluaciones/${evaluacion.id}?inscripcionId=${inscripcionId}`)
            }
          >
            Ir a la evaluación →
          </button>
        </div>
      )}
    </div>
  );
}

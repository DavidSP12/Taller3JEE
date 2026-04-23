import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { evaluacionesApi } from '../services/api';
import { useFetch } from '../hooks/useFetch';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import styles from './Evaluacion.module.css';

export default function EvaluacionPage() {
  const { evalId } = useParams();
  const [searchParams] = useSearchParams();
  const inscripcionId = searchParams.get('inscripcionId');
  const navigate = useNavigate();

  const [respuestas, setRespuestas] = useState({});
  const [resultado, setResultado] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const { data: evaluacion, loading } = useFetch(
    () => evaluacionesApi.getEvaluacion(evalId), [evalId]
  );

  const handleSelect = (preguntaId, opcion) => {
    setRespuestas((prev) => ({ ...prev, [preguntaId]: opcion }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!inscripcionId) return setError('No tienes inscripción activa.');
    const payload = {
      respuestas: Object.entries(respuestas).map(([preguntaId, respuesta]) => ({
        preguntaId: Number(preguntaId),
        respuesta,
      })),
    };
    setSubmitting(true);
    setError('');
    try {
      const { data } = await evaluacionesApi.responder(inscripcionId, evalId, payload);
      setResultado(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al enviar respuestas.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Spinner message="Cargando evaluación…" />;

  if (resultado) {
    const aprobado = resultado.aprobado;
    return (
      <div className={styles.resultCard}>
        <div className={`${styles.resultIcon} ${aprobado ? styles.pass : styles.fail}`}>
          {aprobado ? '🎉' : '😞'}
        </div>
        <h1 className={styles.resultTitle}>
          {aprobado ? '¡Aprobaste!' : 'No aprobaste esta vez'}
        </h1>
        <p className={styles.resultScore}>
          {resultado.puntajeObtenido.toFixed(1)} / {resultado.puntajeMaximo} pts
          ({resultado.porcentaje.toFixed(1)}%)
        </p>
        <p className={styles.resultHint}>
          {aprobado
            ? 'Excelente trabajo. Continúa con la siguiente clase.'
            : 'Repasa los contenidos e intenta de nuevo.'}
        </p>
        <button className={styles.btnBack} onClick={() => navigate(-1)}>
          Volver a la clase
        </button>
      </div>
    );
  }

  return (
    <div>
      <button className={styles.back} onClick={() => navigate(-1)}>← Volver</button>
      <h1 className={styles.title}>{evaluacion?.titulo}</h1>
      <p className={styles.meta}>
        {evaluacion?.preguntas?.length} preguntas · Puntaje máximo: {evaluacion?.puntajeMaximo}
      </p>

      <Alert type="error" message={error} />

      <form onSubmit={handleSubmit}>
        {(evaluacion?.preguntas || []).map((p, idx) => (
          <div key={p.id} className={styles.pregunta}>
            <h3 className={styles.enunciado}>
              <span className={styles.num}>{idx + 1}.</span> {p.enunciado}
            </h3>
            <div className={styles.opciones}>
              {(p.opciones || []).map((op) => (
                <label key={op} className={`${styles.opcion} ${respuestas[p.id] === op ? styles.selected : ''}`}>
                  <input
                    type="radio"
                    name={`p-${p.id}`}
                    value={op}
                    checked={respuestas[p.id] === op}
                    onChange={() => handleSelect(p.id, op)}
                    required
                  />
                  {op}
                </label>
              ))}
            </div>
          </div>
        ))}
        <button className={styles.btnSubmit} type="submit" disabled={submitting}>
          {submitting ? 'Enviando…' : 'Enviar Respuestas'}
        </button>
      </form>
    </div>
  );
}

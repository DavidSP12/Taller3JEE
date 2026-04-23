import { useState } from 'react';
import { adminApi } from '../services/api';
import Alert from '../components/ui/Alert';
import styles from './Admin.module.css';

export default function AdminPage() {
  const [jobMsg, setJobMsg] = useState('');
  const [simResult, setSimResult] = useState(null);
  const [loadingJob, setLoadingJob] = useState(false);
  const [loadingSim, setLoadingSim] = useState(false);

  const [simForm, setSimForm] = useState({
    estudianteId: '',
    perfilAprendizaje: 'PROMEDIO',
    tasaError: 0.2,
    clasesASimular: 5,
    seed: 42,
  });

  const handleCargar = async () => {
    setLoadingJob(true);
    setJobMsg('');
    try {
      const { data } = await adminApi.cargarCurso();
      setJobMsg(`✅ Job lanzado. ID: ${data.jobId} · Estado: ${data.status}`);
    } catch (e) {
      setJobMsg('❌ ' + (e.response?.data?.error || e.message));
    } finally {
      setLoadingJob(false);
    }
  };

  const handleSim = async (e) => {
    e.preventDefault();
    setLoadingSim(true);
    setSimResult(null);
    try {
      const { data } = await adminApi.ejecutarSimulacion({
        ...simForm,
        estudianteId: Number(simForm.estudianteId),
        tasaError: Number(simForm.tasaError),
        clasesASimular: Number(simForm.clasesASimular),
        seed: Number(simForm.seed),
      });
      setSimResult(data);
    } catch (e) {
      setSimResult({ error: e.response?.data?.message || e.message });
    } finally {
      setLoadingSim(false);
    }
  };

  return (
    <div>
      <h1 className={styles.title}>Panel de Administración</h1>

      {/* Batch */}
      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>🗂 Carga de Curso (Batch)</h2>
        <p className={styles.sectionDesc}>
          Ejecuta el job de Spring Batch para cargar el <code>curso-manifest.json</code>.
        </p>
        <button className={styles.btn} onClick={handleCargar} disabled={loadingJob}>
          {loadingJob ? 'Ejecutando…' : 'Cargar Curso'}
        </button>
        {jobMsg && <Alert type={jobMsg.startsWith('✅') ? 'success' : 'error'} message={jobMsg} />}
      </section>

      {/* Simulation */}
      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>🤖 Simulación de Estudiante</h2>
        <form onSubmit={handleSim} className={styles.form}>
          <label className={styles.label}>
            ID del Estudiante
            <input className={styles.input} type="number" required
              value={simForm.estudianteId}
              onChange={(e) => setSimForm({ ...simForm, estudianteId: e.target.value })} />
          </label>
          <label className={styles.label}>
            Perfil de Aprendizaje
            <select className={styles.input}
              value={simForm.perfilAprendizaje}
              onChange={(e) => setSimForm({ ...simForm, perfilAprendizaje: e.target.value })}>
              <option value="RAPIDO">RAPIDO</option>
              <option value="PROMEDIO">PROMEDIO</option>
              <option value="LENTO">LENTO</option>
            </select>
          </label>
          <label className={styles.label}>
            Tasa de Error (0–1)
            <input className={styles.input} type="number" min="0" max="1" step="0.05"
              value={simForm.tasaError}
              onChange={(e) => setSimForm({ ...simForm, tasaError: e.target.value })} />
          </label>
          <label className={styles.label}>
            Clases a Simular
            <input className={styles.input} type="number" min="1" max="35"
              value={simForm.clasesASimular}
              onChange={(e) => setSimForm({ ...simForm, clasesASimular: e.target.value })} />
          </label>
          <label className={styles.label}>
            Semilla (seed)
            <input className={styles.input} type="number"
              value={simForm.seed}
              onChange={(e) => setSimForm({ ...simForm, seed: e.target.value })} />
          </label>
          <button className={styles.btn} type="submit" disabled={loadingSim}>
            {loadingSim ? 'Simulando…' : 'Ejecutar Simulación'}
          </button>
        </form>

        {simResult && (
          <div className={styles.result}>
            {'error' in simResult ? (
              <Alert type="error" message={simResult.error} />
            ) : (
              <table className={styles.table}>
                <tbody>
                  {Object.entries(simResult).map(([k, v]) => (
                    <tr key={k}>
                      <td className={styles.tdKey}>{k}</td>
                      <td className={styles.tdVal}>{String(v)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </section>
    </div>
  );
}

import axios from 'axios';

const BASE_URL = '/api/v1';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Redirect to login on 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ─── Auth ────────────────────────────────────────────────────────────────────
export const authApi = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (data) => api.post('/auth/register', data),
};

// ─── Cursos ──────────────────────────────────────────────────────────────────
export const cursosApi = {
  getCursos: () => api.get('/cursos'),
  getCurso: (id) => api.get(`/cursos/${id}`),
  getClasesByCurso: (cursoId) => api.get(`/cursos/${cursoId}/clases`),
};

// ─── Clases ──────────────────────────────────────────────────────────────────
export const clasesApi = {
  getClase: (id) => api.get(`/clases/${id}`),
  getContenidos: (claseId) => api.get(`/clases/${claseId}/contenidos`),
  getEvaluacion: (claseId) => api.get(`/clases/${claseId}/evaluacion`),
};

// ─── Contenidos ──────────────────────────────────────────────────────────────
export const contenidosApi = {
  getRecursoUrl: (id) => api.get(`/contenidos/${id}/recurso`),
};

// ─── Inscripciones / Progreso ────────────────────────────────────────────────
export const inscripcionApi = {
  inscribir: (estudianteId, cursoId) =>
    api.post('/inscripciones', { estudianteId, cursoId }),
  getByEstudiante: (estudianteId) =>
    api.get(`/inscripciones/estudiante/${estudianteId}`),
  getProgreso: (inscripcionId) => api.get(`/inscripciones/${inscripcionId}/progreso`),
  getProgresoClase: (inscripcionId, claseId) =>
    api.get(`/inscripciones/${inscripcionId}/clases/${claseId}/progreso`),
  iniciarClase: (inscripcionId, claseId) =>
    api.post(`/inscripciones/${inscripcionId}/clases/${claseId}/iniciar`),
  completarClase: (inscripcionId, claseId) =>
    api.post(`/inscripciones/${inscripcionId}/clases/${claseId}/completar`),
  registrarInteraccion: (inscripcionId, contenidoId, data) =>
    api.post(`/inscripciones/${inscripcionId}/contenidos/${contenidoId}/interaccion`, data),
};

// ─── Evaluaciones ─────────────────────────────────────────────────────────────
export const evaluacionesApi = {
  getEvaluacion: (id) => api.get(`/evaluaciones/${id}`),
  responder: (inscripcionId, evalId, respuestas) =>
    api.post(`/inscripciones/${inscripcionId}/evaluaciones/${evalId}/responder`, respuestas),
  getResultado: (inscripcionId, evalId) =>
    api.get(`/inscripciones/${inscripcionId}/evaluaciones/${evalId}/resultado`),
};

// ─── Recomendaciones ─────────────────────────────────────────────────────────
export const recomendacionesApi = {
  getRecomendaciones: (inscripcionId) =>
    api.get(`/inscripciones/${inscripcionId}/recomendaciones`),
  actualizarEstado: (recomendacionId, estado) =>
    api.patch(`/recomendaciones/${recomendacionId}/estado`, { estado }),
};

// ─── Admin ────────────────────────────────────────────────────────────────────
export const adminApi = {
  cargarCurso: () => api.post('/admin/batch/cargar-curso'),
  getJobStatus: (jobId) => api.get(`/admin/batch/jobs/${jobId}/status`),
  ejecutarSimulacion: (data) => api.post('/admin/simulacion', data),
};

export default api;

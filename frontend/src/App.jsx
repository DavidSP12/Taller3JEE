import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ui/ProtectedRoute';
import Layout from './components/layout/Layout';

import LoginPage from './pages/Login';
import RegisterPage from './pages/Register';
import CursosPage from './pages/Cursos';
import ClasesPage from './pages/Clases';
import ClaseDetallePage from './pages/ClaseDetalle';
import EvaluacionPage from './pages/Evaluacion';
import ProgresoPage from './pages/Progreso';
import RecomendacionesPage from './pages/Recomendaciones';
import AdminPage from './pages/Admin';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected routes wrapped in Layout */}
          <Route path="/" element={
            <ProtectedRoute>
              <Layout><CursosPage /></Layout>
            </ProtectedRoute>
          } />

          <Route path="/cursos/:cursoId/clases" element={
            <ProtectedRoute>
              <Layout><ClasesPage /></Layout>
            </ProtectedRoute>
          } />

          <Route path="/clases/:claseId" element={
            <ProtectedRoute>
              <Layout><ClaseDetallePage /></Layout>
            </ProtectedRoute>
          } />

          <Route path="/evaluaciones/:evalId" element={
            <ProtectedRoute>
              <Layout><EvaluacionPage /></Layout>
            </ProtectedRoute>
          } />

          <Route path="/inscripciones/:inscripcionId/progreso" element={
            <ProtectedRoute>
              <Layout><ProgresoPage /></Layout>
            </ProtectedRoute>
          } />

          <Route path="/inscripciones/:inscripcionId/recomendaciones" element={
            <ProtectedRoute>
              <Layout><RecomendacionesPage /></Layout>
            </ProtectedRoute>
          } />

          <Route path="/admin" element={
            <ProtectedRoute adminOnly>
              <Layout><AdminPage /></Layout>
            </ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}


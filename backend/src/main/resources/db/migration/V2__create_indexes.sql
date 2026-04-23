-- V2__create_indexes.sql

CREATE INDEX idx_progresoclase_inscripcion ON progreso_clase(inscripcion_id);
CREATE INDEX idx_interaccion_inscripcion_contenido ON interaccion_contenido(inscripcion_id, contenido_id);
CREATE INDEX idx_resultado_evaluacion_inscripcion ON resultado_evaluacion(inscripcion_id, evaluacion_id);
CREATE INDEX idx_recomendacion_inscripcion_estado ON recomendacion(inscripcion_id, estado);
CREATE INDEX idx_clase_curso ON clase(curso_id);
CREATE INDEX idx_contenido_clase ON contenido(clase_id);
CREATE INDEX idx_inscripcion_estudiante ON inscripcion(estudiante_id);
CREATE INDEX idx_inscripcion_curso ON inscripcion(curso_id);

package com.taller3jee.service;

import com.taller3jee.domain.*;
import com.taller3jee.dto.RecomendacionDTO;
import com.taller3jee.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecomendacionService {

    private final RecomendacionRepository recomendacionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ProgresoClaseRepository progresoClaseRepository;
    private final ResultadoEvaluacionRepository resultadoEvaluacionRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final InteraccionContenidoRepository interaccionContenidoRepository;
    private final ContenidoRepository contenidoRepository;
    private final ClaseRepository claseRepository;

    @Value("${app.recommendation.evaluacion-fallo-threshold:2}")
    private int falloThreshold;

    @Value("${app.recommendation.tiempo-factor-threshold:2.0}")
    private double tiempoFactorThreshold;

    @Value("${app.recommendation.completitud-threshold:0.60}")
    private double completitudThreshold;

    @Transactional(readOnly = true)
    public List<RecomendacionDTO> getRecomendaciones(Long inscripcionId) {
        findInscripcion(inscripcionId);
        return recomendacionRepository
                .findByInscripcionIdAndEstado(inscripcionId, EstadoRecomendacion.PENDIENTE)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public RecomendacionDTO actualizarEstado(Long recomendacionId, EstadoRecomendacion estado) {
        Recomendacion r = recomendacionRepository.findById(recomendacionId)
                .orElseThrow(() -> new EntityNotFoundException("Recomendacion not found: " + recomendacionId));
        r.setEstado(estado);
        return toDTO(recomendacionRepository.save(r));
    }

    @Transactional
    public void generarRecomendacion(Long inscripcionId, Long claseId) {
        Inscripcion inscripcion = findInscripcion(inscripcionId);
        Clase clase = claseRepository.findById(claseId)
                .orElseThrow(() -> new EntityNotFoundException("Clase not found: " + claseId));

        // Rule 1: Failed evaluacion >= threshold times
        evaluacionRepository.findByClaseId(claseId).ifPresent(eval -> {
            double passingScore = eval.getPuntajeMaximo() * 0.7;
            long fallos = resultadoEvaluacionRepository
                    .countByInscripcionIdAndEvaluacionIdAndPuntajeObtenidoLessThan(inscripcionId, eval.getId(), passingScore);
            if (fallos >= falloThreshold) {
                saveRecomendacion(inscripcion, TipoRecomendacion.REFUERZO, clase, null,
                        "Ha fallado la evaluación " + fallos + " veces. Se recomienda reforzar los contenidos.",
                        "ALTA");
                return;
            }
        });

        // Rule 2: Time in class > 2x avg of other students
        Long myTime = interaccionContenidoRepository.sumDuracionByInscripcionIdAndClaseId(inscripcionId, claseId);
        Double avgTime = interaccionContenidoRepository.avgDuracionByClaseId(claseId);
        if (myTime != null && avgTime != null && avgTime > 0 && myTime > tiempoFactorThreshold * avgTime) {
            List<Contenido> contenidos = contenidoRepository.findByClaseIdOrderByOrdenEnClase(claseId);
            Contenido alternativo = contenidos.isEmpty() ? null : contenidos.get(0);
            saveRecomendacion(inscripcion, TipoRecomendacion.REFUERZO, clase, alternativo,
                    "El tiempo dedicado a esta clase supera ampliamente el promedio. Prueba con este recurso alternativo.",
                    "MEDIA");
            return;
        }

        // Rule 3: completitud < 60%
        List<Contenido> contenidos = contenidoRepository.findByClaseIdOrderByOrdenEnClase(claseId);
        long completados = contenidos.stream()
                .filter(c -> interaccionContenidoRepository
                        .findByInscripcionIdAndContenidoId(inscripcionId, c.getId())
                        .map(i -> Boolean.TRUE.equals(i.getCompletado()))
                        .orElse(false))
                .count();
        double completitud = contenidos.isEmpty() ? 1.0 : (double) completados / contenidos.size();
        if (completitud < completitudThreshold) {
            saveRecomendacion(inscripcion, TipoRecomendacion.REFUERZO, clase, null,
                    "Has completado solo el " + (int)(completitud * 100) + "% de los contenidos. Revisa los pendientes.",
                    "MEDIA");
            return;
        }

        // Rule 4: Clase completada con puntaje >= 70%
        progresoClaseRepository.findByInscripcionIdAndClaseId(inscripcionId, claseId).ifPresent(prog -> {
            if (prog.getEstado() == EstadoProgreso.COMPLETADO) {
                evaluacionRepository.findByClaseId(claseId).ifPresent(eval -> {
                    List<ResultadoEvaluacion> resultados = resultadoEvaluacionRepository
                            .findByInscripcionIdAndEvaluacionId(inscripcionId, eval.getId());
                    if (!resultados.isEmpty()) {
                        ResultadoEvaluacion ultimo = resultados.get(resultados.size() - 1);
                        double pct = eval.getPuntajeMaximo() > 0
                                ? (ultimo.getPuntajeObtenido() / eval.getPuntajeMaximo()) * 100
                                : 0.0;
                        if (pct >= 70.0) {
                            List<Clase> clases = claseRepository.findByCursoIdOrderByNumero(inscripcion.getCurso().getId());
                            clases.stream()
                                    .filter(c -> c.getNumero() == clase.getNumero() + 1)
                                    .findFirst()
                                    .ifPresent(siguiente -> saveRecomendacion(inscripcion, TipoRecomendacion.SIGUIENTE_TEMA,
                                            siguiente, null,
                                            "¡Excelente! Aprobaste con " + (int) pct + "%. Continúa con el siguiente tema.",
                                            "BAJA"));
                        }
                    }
                });
            }
        });

        // Rule 5: Always save at least one recommendation
        boolean hasAny = !recomendacionRepository.findByInscripcionId(inscripcionId).isEmpty();
        if (!hasAny) {
            saveRecomendacion(inscripcion, TipoRecomendacion.RECORDATORIO, clase, null,
                    "Recuerda continuar con tu aprendizaje en esta clase.", "BAJA");
        }
    }

    private void saveRecomendacion(Inscripcion inscripcion, TipoRecomendacion tipo,
                                    Clase clase, Contenido contenido, String motivo, String prioridad) {
        Recomendacion r = new Recomendacion();
        r.setInscripcion(inscripcion);
        r.setTipo(tipo);
        r.setClase(clase);
        r.setContenido(contenido);
        r.setMotivo(motivo);
        r.setEstado(EstadoRecomendacion.PENDIENTE);
        r.setFechaGenerada(LocalDateTime.now());
        r.setPrioridad(prioridad);
        recomendacionRepository.save(r);
        log.debug("Recommendation saved: type={} for inscripcion={}", tipo, inscripcion.getId());
    }

    private Inscripcion findInscripcion(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inscripcion not found: " + id));
    }

    private RecomendacionDTO toDTO(Recomendacion r) {
        RecomendacionDTO dto = new RecomendacionDTO();
        dto.setId(r.getId());
        dto.setInscripcionId(r.getInscripcion().getId());
        dto.setTipo(r.getTipo());
        if (r.getClase() != null) {
            dto.setClaseId(r.getClase().getId());
            dto.setClaseTitulo(r.getClase().getTitulo());
        }
        if (r.getContenido() != null) {
            dto.setContenidoId(r.getContenido().getId());
        }
        dto.setMotivo(r.getMotivo());
        dto.setEstado(r.getEstado());
        dto.setFechaGenerada(r.getFechaGenerada());
        dto.setPrioridad(r.getPrioridad());
        return dto;
    }
}

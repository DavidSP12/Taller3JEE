package com.taller3jee.service;

import com.taller3jee.domain.*;
import com.taller3jee.dto.SimulacionRequest;
import com.taller3jee.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulacionService {

    private final InscripcionRepository inscripcionRepository;
    private final ClaseRepository claseRepository;
    private final ContenidoRepository contenidoRepository;
    private final ProgresoClaseRepository progresoClaseRepository;
    private final InteraccionContenidoRepository interaccionContenidoRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final PreguntaRepository preguntaRepository;
    private final ResultadoEvaluacionRepository resultadoEvaluacionRepository;
    private final RecomendacionService recomendacionService;
    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;

    @Transactional
    public Map<String, Object> ejecutarSimulacion(SimulacionRequest req) {
        Estudiante estudiante = estudianteRepository.findById(req.getEstudianteId())
                .orElseThrow(() -> new EntityNotFoundException("Estudiante not found: " + req.getEstudianteId()));

        // Find or create inscripcion for first available course
        List<Curso> cursos = cursoRepository.findAll();
        if (cursos.isEmpty()) {
            throw new IllegalStateException("No courses available for simulation");
        }
        Curso curso = cursos.get(0);

        Inscripcion inscripcion = inscripcionRepository
                .findByEstudianteIdAndCursoId(req.getEstudianteId(), curso.getId())
                .orElseGet(() -> {
                    Inscripcion i = new Inscripcion();
                    i.setEstudiante(estudiante);
                    i.setCurso(curso);
                    i.setFechaInicio(LocalDateTime.now());
                    i.setEstado(EstadoInscripcion.EN_PROGRESO);
                    return inscripcionRepository.save(i);
                });

        List<Clase> clases = claseRepository.findByCursoIdOrderByNumero(curso.getId());
        int limit = Math.min(req.getClasesASimular(), clases.size());
        Random random = new Random(req.getSeed());

        int clasesCompletadas = 0;
        int evaluacionesAprobadas = 0;
        int evaluacionesFalladas = 0;

        for (int idx = 0; idx < limit; idx++) {
            Clase clase = clases.get(idx);

            // Step 1: Create ProgresoClase EN_PROGRESO
            ProgresoClase progreso = progresoClaseRepository
                    .findByInscripcionIdAndClaseId(inscripcion.getId(), clase.getId())
                    .orElseGet(() -> {
                        ProgresoClase p = new ProgresoClase();
                        p.setInscripcion(inscripcion);
                        p.setClase(clase);
                        return p;
                    });
            progreso.setEstado(EstadoProgreso.EN_PROGRESO);
            progreso.setFechaInicio(LocalDateTime.now().minusHours(limit - idx));
            progresoClaseRepository.save(progreso);

            // Step 2: Create InteraccionContenido for each content
            List<Contenido> contenidos = contenidoRepository.findByClaseIdOrderByOrdenEnClase(clase.getId());
            long totalDuracion = 0;
            for (Contenido c : contenidos) {
                long duracion = estimateDuration(c, req.getPerfilAprendizaje(), random);
                totalDuracion += duracion;

                InteraccionContenido interaccion = interaccionContenidoRepository
                        .findByInscripcionIdAndContenidoId(inscripcion.getId(), c.getId())
                        .orElseGet(() -> {
                            InteraccionContenido i = new InteraccionContenido();
                            i.setInscripcion(inscripcion);
                            i.setContenido(c);
                            return i;
                        });
                interaccion.setFechaAcceso(LocalDateTime.now().minusHours(limit - idx));
                interaccion.setDuracionSegundos(duracion);
                interaccion.setCompletado(random.nextDouble() > req.getTasaError());
                interaccionContenidoRepository.save(interaccion);
            }

            // Step 3: If Evaluacion exists, calculate score
            Optional<Evaluacion> evalOpt = evaluacionRepository.findByClaseId(clase.getId());
            boolean aprobado = true;
            if (evalOpt.isPresent()) {
                Evaluacion eval = evalOpt.get();
                List<Pregunta> preguntas = preguntaRepository.findByEvaluacionId(eval.getId());
                int intento = (int) resultadoEvaluacionRepository
                        .findByInscripcionIdAndEvaluacionId(inscripcion.getId(), eval.getId()).size() + 1;

                int correctas = 0;
                for (Pregunta p : preguntas) {
                    if (random.nextDouble() > req.getTasaError()) {
                        correctas++;
                    }
                }
                double puntaje = preguntas.isEmpty() ? eval.getPuntajeMaximo()
                        : ((double) correctas / preguntas.size()) * eval.getPuntajeMaximo();
                double pct = eval.getPuntajeMaximo() > 0 ? (puntaje / eval.getPuntajeMaximo()) * 100 : 0;
                aprobado = pct >= 70.0;

                ResultadoEvaluacion resultado = new ResultadoEvaluacion();
                resultado.setInscripcion(inscripcion);
                resultado.setEvaluacion(eval);
                resultado.setPuntajeObtenido(puntaje);
                resultado.setPuntajeMaximo(eval.getPuntajeMaximo());
                resultado.setIntento(intento);
                resultado.setFechaRealizacion(LocalDateTime.now());
                resultadoEvaluacionRepository.save(resultado);

                if (aprobado) evaluacionesAprobadas++; else evaluacionesFalladas++;
            }

            // Step 4: Update ProgresoClase
            if (aprobado) {
                progreso.setEstado(EstadoProgreso.COMPLETADO);
                progreso.setFechaCompletado(LocalDateTime.now());
                progreso.setTiempoTotalSegundos(totalDuracion);
                progresoClaseRepository.save(progreso);
                clasesCompletadas++;
            }

            // Step 5: Generate recommendation
            try {
                recomendacionService.generarRecomendacion(inscripcion.getId(), clase.getId());
            } catch (Exception e) {
                log.warn("Could not generate recommendation for clase {}: {}", clase.getId(), e.getMessage());
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("inscripcionId", inscripcion.getId());
        summary.put("estudianteId", req.getEstudianteId());
        summary.put("cursoId", curso.getId());
        summary.put("clasesSimuladas", limit);
        summary.put("clasesCompletadas", clasesCompletadas);
        summary.put("evaluacionesAprobadas", evaluacionesAprobadas);
        summary.put("evaluacionesFalladas", evaluacionesFalladas);
        summary.put("perfilAprendizaje", req.getPerfilAprendizaje());
        summary.put("tasaError", req.getTasaError());
        return summary;
    }

    private long estimateDuration(Contenido contenido, String perfil, Random random) {
        int baseSeconds = switch (contenido.getTipo()) {
            case VIDEO -> 900;
            case PDF -> 600;
            case TEXT -> 300;
            case PPTX -> 480;
            case IMAGE -> 120;
            case URL -> 240;
            default -> 360;
        };
        double factor = switch (perfil != null ? perfil.toUpperCase() : "PROMEDIO") {
            case "RAPIDO" -> 0.6;
            case "LENTO" -> 1.6;
            default -> 1.0;
        };
        double variation = 0.8 + random.nextDouble() * 0.4;
        return (long) (baseSeconds * factor * variation);
    }
}

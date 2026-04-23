package com.taller3jee.service;

import com.taller3jee.domain.*;
import com.taller3jee.dto.EvaluacionDTO;
import com.taller3jee.dto.PreguntaDTO;
import com.taller3jee.dto.ResultadoEvaluacionDTO;
import com.taller3jee.dto.RespuestasRequest;
import com.taller3jee.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaEstudianteRepository respuestaEstudianteRepository;
    private final ResultadoEvaluacionRepository resultadoEvaluacionRepository;
    private final InscripcionRepository inscripcionRepository;

    @Transactional(readOnly = true)
    public EvaluacionDTO getEvaluacionByClaseId(Long claseId) {
        Evaluacion evaluacion = evaluacionRepository.findByClaseId(claseId)
                .orElseThrow(() -> new EntityNotFoundException("No evaluation for clase: " + claseId));
        return toEvaluacionDTO(evaluacion);
    }

    @Transactional(readOnly = true)
    public EvaluacionDTO getEvaluacion(Long id) {
        Evaluacion evaluacion = evaluacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evaluacion not found: " + id));
        return toEvaluacionDTO(evaluacion);
    }

    @Transactional
    public ResultadoEvaluacionDTO responderEvaluacion(Long inscripcionId, Long evalId, RespuestasRequest req) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new EntityNotFoundException("Inscripcion not found: " + inscripcionId));
        Evaluacion evaluacion = evaluacionRepository.findById(evalId)
                .orElseThrow(() -> new EntityNotFoundException("Evaluacion not found: " + evalId));

        int intento = (int) resultadoEvaluacionRepository
                .findByInscripcionIdAndEvaluacionId(inscripcionId, evalId).size() + 1;

        List<Pregunta> preguntas = preguntaRepository.findByEvaluacionId(evalId);
        int correctas = 0;

        for (RespuestasRequest.RespuestaItem item : req.getRespuestas()) {
            preguntas.stream()
                    .filter(p -> p.getId().equals(item.getPreguntaId()))
                    .findFirst()
                    .ifPresent(pregunta -> {
                        RespuestaEstudiante resp = new RespuestaEstudiante();
                        resp.setInscripcion(inscripcion);
                        resp.setEvaluacion(evaluacion);
                        resp.setPregunta(pregunta);
                        resp.setRespuesta(item.getRespuesta());
                        boolean esCorrecta = pregunta.getRespuestaCorrecta() != null
                                && pregunta.getRespuestaCorrecta().equalsIgnoreCase(item.getRespuesta());
                        resp.setCorrecta(esCorrecta);
                        resp.setFechaRespuesta(LocalDateTime.now());
                        resp.setIntento(intento);
                        respuestaEstudianteRepository.save(resp);
                    });

            boolean esCorrecta = preguntas.stream()
                    .filter(p -> p.getId().equals(item.getPreguntaId()))
                    .findFirst()
                    .map(p -> p.getRespuestaCorrecta() != null
                            && p.getRespuestaCorrecta().equalsIgnoreCase(item.getRespuesta()))
                    .orElse(false);
            if (esCorrecta) correctas++;
        }

        double puntaje = preguntas.isEmpty() ? 0.0
                : ((double) correctas / preguntas.size()) * evaluacion.getPuntajeMaximo();

        ResultadoEvaluacion resultado = new ResultadoEvaluacion();
        resultado.setInscripcion(inscripcion);
        resultado.setEvaluacion(evaluacion);
        resultado.setPuntajeObtenido(puntaje);
        resultado.setPuntajeMaximo(evaluacion.getPuntajeMaximo());
        resultado.setIntento(intento);
        resultado.setFechaRealizacion(LocalDateTime.now());
        resultadoEvaluacionRepository.save(resultado);

        return toResultadoDTO(resultado);
    }

    @Transactional(readOnly = true)
    public ResultadoEvaluacionDTO getResultado(Long inscripcionId, Long evalId) {
        List<ResultadoEvaluacion> resultados = resultadoEvaluacionRepository
                .findByInscripcionIdAndEvaluacionId(inscripcionId, evalId);
        if (resultados.isEmpty()) {
            throw new EntityNotFoundException("No result found for inscripcion " + inscripcionId + " evaluacion " + evalId);
        }
        return toResultadoDTO(resultados.get(resultados.size() - 1));
    }

    private EvaluacionDTO toEvaluacionDTO(Evaluacion e) {
        EvaluacionDTO dto = new EvaluacionDTO();
        dto.setId(e.getId());
        dto.setClaseId(e.getClase().getId());
        dto.setTitulo(e.getTitulo());
        dto.setPuntajeMaximo(e.getPuntajeMaximo());
        List<PreguntaDTO> preguntaDTOs = preguntaRepository.findByEvaluacionId(e.getId()).stream()
                .map(this::toPreguntaDTO)
                .toList();
        dto.setPreguntas(preguntaDTOs);
        return dto;
    }

    private PreguntaDTO toPreguntaDTO(Pregunta p) {
        PreguntaDTO dto = new PreguntaDTO();
        dto.setId(p.getId());
        dto.setEnunciado(p.getEnunciado());
        dto.setTipo(p.getTipo());
        dto.setOrden(p.getOrden());
        dto.setOpciones(p.getOpciones());
        return dto;
    }

    private ResultadoEvaluacionDTO toResultadoDTO(ResultadoEvaluacion r) {
        ResultadoEvaluacionDTO dto = new ResultadoEvaluacionDTO();
        dto.setId(r.getId());
        dto.setEvaluacionId(r.getEvaluacion().getId());
        dto.setInscripcionId(r.getInscripcion().getId());
        dto.setPuntajeObtenido(r.getPuntajeObtenido());
        dto.setPuntajeMaximo(r.getPuntajeMaximo());
        double porcentaje = r.getPuntajeMaximo() > 0 ? (r.getPuntajeObtenido() / r.getPuntajeMaximo()) * 100 : 0.0;
        dto.setPorcentaje(porcentaje);
        dto.setIntento(r.getIntento());
        dto.setFechaRealizacion(r.getFechaRealizacion());
        dto.setAprobado(porcentaje >= 70.0);
        return dto;
    }
}

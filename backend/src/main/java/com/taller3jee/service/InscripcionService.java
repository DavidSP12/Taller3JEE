package com.taller3jee.service;

import com.taller3jee.domain.*;
import com.taller3jee.dto.InteraccionRequest;
import com.taller3jee.dto.ProgresoDTO;
import com.taller3jee.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final ProgresoClaseRepository progresoClaseRepository;
    private final InteraccionContenidoRepository interaccionContenidoRepository;
    private final ClaseRepository claseRepository;
    private final ContenidoRepository contenidoRepository;

    @Transactional(readOnly = true)
    public List<ProgresoDTO> getProgreso(Long inscripcionId) {
        Inscripcion inscripcion = findInscripcion(inscripcionId);
        List<Clase> clases = claseRepository.findByCursoIdOrderByNumero(inscripcion.getCurso().getId());
        int total = clases.size();
        return progresoClaseRepository.findByInscripcionId(inscripcionId).stream()
                .map(p -> toProgresoDTO(p, total))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgresoDTO getProgresoClase(Long inscripcionId, Long claseId) {
        findInscripcion(inscripcionId);
        ProgresoClase progreso = progresoClaseRepository.findByInscripcionIdAndClaseId(inscripcionId, claseId)
                .orElseThrow(() -> new EntityNotFoundException("Progreso not found for inscripcion " + inscripcionId + " clase " + claseId));
        List<Clase> clases = claseRepository.findByCursoIdOrderByNumero(progreso.getInscripcion().getCurso().getId());
        return toProgresoDTO(progreso, clases.size());
    }

    @Transactional
    public ProgresoClase iniciarClase(Long inscripcionId, Long claseId) {
        Inscripcion inscripcion = findInscripcion(inscripcionId);
        Clase clase = claseRepository.findById(claseId)
                .orElseThrow(() -> new EntityNotFoundException("Clase not found: " + claseId));

        ProgresoClase progreso = progresoClaseRepository.findByInscripcionIdAndClaseId(inscripcionId, claseId)
                .orElseGet(() -> {
                    ProgresoClase p = new ProgresoClase();
                    p.setInscripcion(inscripcion);
                    p.setClase(clase);
                    return p;
                });

        if (progreso.getEstado() == null || progreso.getEstado() == EstadoProgreso.NO_INICIADO) {
            progreso.setEstado(EstadoProgreso.EN_PROGRESO);
            progreso.setFechaInicio(LocalDateTime.now());
        }

        return progresoClaseRepository.save(progreso);
    }

    @Transactional
    public ProgresoClase completarClase(Long inscripcionId, Long claseId) {
        findInscripcion(inscripcionId);
        ProgresoClase progreso = progresoClaseRepository.findByInscripcionIdAndClaseId(inscripcionId, claseId)
                .orElseThrow(() -> new EntityNotFoundException("Progreso not found for inscripcion " + inscripcionId + " clase " + claseId));

        Long duracion = interaccionContenidoRepository
                .sumDuracionByInscripcionIdAndClaseId(inscripcionId, claseId);

        progreso.setEstado(EstadoProgreso.COMPLETADO);
        progreso.setFechaCompletado(LocalDateTime.now());
        progreso.setTiempoTotalSegundos(duracion != null ? duracion : 0L);

        return progresoClaseRepository.save(progreso);
    }

    @Transactional
    public InteraccionContenido registrarInteraccion(Long inscripcionId, Long contenidoId, InteraccionRequest req) {
        Inscripcion inscripcion = findInscripcion(inscripcionId);
        Contenido contenido = contenidoRepository.findById(contenidoId)
                .orElseThrow(() -> new EntityNotFoundException("Contenido not found: " + contenidoId));

        InteraccionContenido interaccion = interaccionContenidoRepository
                .findByInscripcionIdAndContenidoId(inscripcionId, contenidoId)
                .orElseGet(() -> {
                    InteraccionContenido i = new InteraccionContenido();
                    i.setInscripcion(inscripcion);
                    i.setContenido(contenido);
                    return i;
                });

        interaccion.setFechaAcceso(LocalDateTime.now());
        interaccion.setDuracionSegundos(req.getDuracionSegundos());
        interaccion.setCompletado(req.getCompletado());

        return interaccionContenidoRepository.save(interaccion);
    }

    private Inscripcion findInscripcion(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inscripcion not found: " + id));
    }

    private ProgresoDTO toProgresoDTO(ProgresoClase p, int totalClases) {
        ProgresoDTO dto = new ProgresoDTO();
        dto.setId(p.getId());
        dto.setClaseId(p.getClase().getId());
        dto.setClaseTitulo(p.getClase().getTitulo());
        dto.setEstado(p.getEstado());
        dto.setFechaInicio(p.getFechaInicio());
        dto.setFechaCompletado(p.getFechaCompletado());
        dto.setTiempoTotalSegundos(p.getTiempoTotalSegundos());
        double pct = totalClases > 0
                ? (p.getEstado() == EstadoProgreso.COMPLETADO ? 100.0 : p.getEstado() == EstadoProgreso.EN_PROGRESO ? 50.0 : 0.0)
                : 0.0;
        dto.setPct(pct);
        return dto;
    }
}

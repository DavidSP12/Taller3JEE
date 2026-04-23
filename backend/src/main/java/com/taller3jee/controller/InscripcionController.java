package com.taller3jee.controller;

import com.taller3jee.domain.Curso;
import com.taller3jee.domain.Estudiante;
import com.taller3jee.domain.EstadoInscripcion;
import com.taller3jee.domain.Inscripcion;
import com.taller3jee.dto.InscripcionRequest;
import com.taller3jee.dto.InteraccionRequest;
import com.taller3jee.dto.InscripcionDTO;
import com.taller3jee.dto.ProgresoDTO;
import com.taller3jee.repository.CursoRepository;
import com.taller3jee.repository.EstudianteRepository;
import com.taller3jee.repository.InscripcionRepository;
import com.taller3jee.service.InscripcionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;

    @PostMapping
    public ResponseEntity<InscripcionDTO> inscribir(@RequestBody InscripcionRequest body) {
        Long estudianteId = body.getEstudianteId();
        Long cursoId = body.getCursoId();
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new EntityNotFoundException("Estudiante not found: " + estudianteId));
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new EntityNotFoundException("Curso not found: " + cursoId));

        Inscripcion inscripcion = inscripcionRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId)
                .orElseGet(() -> {
                    Inscripcion i = new Inscripcion();
                    i.setEstudiante(estudiante);
                    i.setCurso(curso);
                    i.setFechaInicio(LocalDateTime.now());
                    i.setEstado(EstadoInscripcion.EN_PROGRESO);
                    return inscripcionRepository.save(i);
                });
        return ResponseEntity.ok(toInscripcionDTO(inscripcion));
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<InscripcionDTO>> getByEstudiante(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(
                inscripcionRepository.findByEstudianteId(estudianteId).stream()
                        .map(this::toInscripcionDTO).toList()
        );
    }

    @GetMapping("/{id}/progreso")
    public ResponseEntity<List<ProgresoDTO>> getProgreso(@PathVariable Long id) {
        return ResponseEntity.ok(inscripcionService.getProgreso(id));
    }

    @GetMapping("/{id}/clases/{claseId}/progreso")
    public ResponseEntity<ProgresoDTO> getProgresoClase(@PathVariable Long id,
                                                         @PathVariable Long claseId) {
        return ResponseEntity.ok(inscripcionService.getProgresoClase(id, claseId));
    }

    @PostMapping("/{id}/clases/{claseId}/iniciar")
    public ResponseEntity<Map<String, Object>> iniciarClase(@PathVariable Long id,
                                                             @PathVariable Long claseId) {
        var progreso = inscripcionService.iniciarClase(id, claseId);
        return ResponseEntity.ok(Map.of(
                "progresoId", progreso.getId(),
                "estado", progreso.getEstado(),
                "fechaInicio", progreso.getFechaInicio().toString()
        ));
    }

    @PostMapping("/{id}/clases/{claseId}/completar")
    public ResponseEntity<Map<String, Object>> completarClase(@PathVariable Long id,
                                                               @PathVariable Long claseId) {
        var progreso = inscripcionService.completarClase(id, claseId);
        return ResponseEntity.ok(Map.of(
                "progresoId", progreso.getId(),
                "estado", progreso.getEstado(),
                "tiempoTotalSegundos", progreso.getTiempoTotalSegundos()
        ));
    }

    @PostMapping("/{id}/contenidos/{contenidoId}/interaccion")
    public ResponseEntity<Map<String, Object>> registrarInteraccion(@PathVariable Long id,
                                                                      @PathVariable Long contenidoId,
                                                                      @RequestBody InteraccionRequest request) {
        var interaccion = inscripcionService.registrarInteraccion(id, contenidoId, request);
        return ResponseEntity.ok(Map.of(
                "interaccionId", interaccion.getId(),
                "completado", Boolean.TRUE.equals(interaccion.getCompletado()),
                "duracionSegundos", interaccion.getDuracionSegundos()
        ));
    }

    private InscripcionDTO toInscripcionDTO(Inscripcion i) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setId(i.getId());
        dto.setEstudianteId(i.getEstudiante().getId());
        dto.setCursoId(i.getCurso().getId());
        dto.setCursoNombre(i.getCurso().getNombre());
        dto.setFechaInicio(i.getFechaInicio());
        dto.setFechaFin(i.getFechaFin());
        dto.setEstado(i.getEstado());
        return dto;
    }
}


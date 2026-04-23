package com.taller3jee.controller;

import com.taller3jee.dto.InteraccionRequest;
import com.taller3jee.dto.ProgresoDTO;
import com.taller3jee.service.InscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;

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
}

package com.taller3jee.controller;

import com.taller3jee.domain.EstadoRecomendacion;
import com.taller3jee.dto.RecomendacionDTO;
import com.taller3jee.service.RecomendacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    @GetMapping("/inscripciones/{id}/recomendaciones")
    public ResponseEntity<List<RecomendacionDTO>> getRecomendaciones(@PathVariable Long id) {
        return ResponseEntity.ok(recomendacionService.getRecomendaciones(id));
    }

    @PatchMapping("/recomendaciones/{id}/estado")
    public ResponseEntity<RecomendacionDTO> actualizarEstado(@PathVariable Long id,
                                                              @RequestBody Map<String, String> body) {
        EstadoRecomendacion estado = EstadoRecomendacion.valueOf(body.get("estado"));
        return ResponseEntity.ok(recomendacionService.actualizarEstado(id, estado));
    }
}

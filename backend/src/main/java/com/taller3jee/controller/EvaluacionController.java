package com.taller3jee.controller;

import com.taller3jee.dto.EvaluacionDTO;
import com.taller3jee.dto.ResultadoEvaluacionDTO;
import com.taller3jee.dto.RespuestasRequest;
import com.taller3jee.service.EvaluacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    @GetMapping("/evaluaciones/{id}")
    public ResponseEntity<EvaluacionDTO> getEvaluacion(@PathVariable Long id) {
        return ResponseEntity.ok(evaluacionService.getEvaluacion(id));
    }

    @PostMapping("/inscripciones/{id}/evaluaciones/{evalId}/responder")
    public ResponseEntity<ResultadoEvaluacionDTO> responderEvaluacion(@PathVariable Long id,
                                                                       @PathVariable Long evalId,
                                                                       @RequestBody RespuestasRequest request) {
        return ResponseEntity.ok(evaluacionService.responderEvaluacion(id, evalId, request));
    }

    @GetMapping("/inscripciones/{id}/evaluaciones/{evalId}/resultado")
    public ResponseEntity<ResultadoEvaluacionDTO> getResultado(@PathVariable Long id,
                                                                @PathVariable Long evalId) {
        return ResponseEntity.ok(evaluacionService.getResultado(id, evalId));
    }
}

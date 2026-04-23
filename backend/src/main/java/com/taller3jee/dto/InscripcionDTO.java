package com.taller3jee.dto;

import com.taller3jee.domain.EstadoInscripcion;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InscripcionDTO {
    private Long id;
    private Long estudianteId;
    private Long cursoId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoInscripcion estado;
}

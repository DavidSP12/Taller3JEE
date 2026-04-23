package com.taller3jee.dto;

import com.taller3jee.domain.EstadoProgreso;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProgresoDTO {
    private Long id;
    private Long claseId;
    private String claseTitulo;
    private EstadoProgreso estado;
    private double pct;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCompletado;
    private Long tiempoTotalSegundos;
}

package com.taller3jee.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResultadoEvaluacionDTO {
    private Long id;
    private Long evaluacionId;
    private Long inscripcionId;
    private Double puntajeObtenido;
    private Integer puntajeMaximo;
    private double porcentaje;
    private Integer intento;
    private LocalDateTime fechaRealizacion;
    private boolean aprobado;
}

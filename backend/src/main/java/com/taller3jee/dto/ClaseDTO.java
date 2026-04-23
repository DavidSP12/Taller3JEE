package com.taller3jee.dto;

import lombok.Data;

@Data
public class ClaseDTO {
    private Long id;
    private Long cursoId;
    private Integer numero;
    private String titulo;
    private String descripcion;
    private Integer orden;
    private Integer duracionEstimadaMin;
}

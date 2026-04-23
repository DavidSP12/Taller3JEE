package com.taller3jee.dto;

import lombok.Data;

import java.util.List;

@Data
public class EvaluacionDTO {
    private Long id;
    private Long claseId;
    private String titulo;
    private Integer puntajeMaximo;
    private List<PreguntaDTO> preguntas;
}

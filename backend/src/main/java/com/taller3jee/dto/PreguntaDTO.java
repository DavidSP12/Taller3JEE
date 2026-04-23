package com.taller3jee.dto;

import com.taller3jee.domain.TipoPregunta;
import lombok.Data;

import java.util.List;

@Data
public class PreguntaDTO {
    private Long id;
    private String enunciado;
    private TipoPregunta tipo;
    private Integer orden;
    private List<String> opciones;
}

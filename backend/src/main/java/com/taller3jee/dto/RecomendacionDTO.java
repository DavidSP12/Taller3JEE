package com.taller3jee.dto;

import com.taller3jee.domain.EstadoRecomendacion;
import com.taller3jee.domain.TipoRecomendacion;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecomendacionDTO {
    private Long id;
    private Long inscripcionId;
    private TipoRecomendacion tipo;
    private Long claseId;
    private String claseTitulo;
    private Long contenidoId;
    private String motivo;
    private EstadoRecomendacion estado;
    private LocalDateTime fechaGenerada;
    private String prioridad;
}

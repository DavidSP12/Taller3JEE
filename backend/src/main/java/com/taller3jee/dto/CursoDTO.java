package com.taller3jee.dto;

import com.taller3jee.domain.EstadoCurso;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CursoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String version;
    private EstadoCurso estado;
    private LocalDateTime fechaCreacion;
}

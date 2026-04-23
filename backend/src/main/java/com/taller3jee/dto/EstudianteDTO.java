package com.taller3jee.dto;

import com.taller3jee.domain.EstadoEstudiante;
import com.taller3jee.domain.RolUsuario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EstudianteDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private LocalDateTime fechaRegistro;
    private EstadoEstudiante estado;
    private RolUsuario rol;
}

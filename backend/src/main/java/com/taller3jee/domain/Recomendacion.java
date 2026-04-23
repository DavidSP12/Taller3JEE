package com.taller3jee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "recomendacion")
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecomendacion tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id")
    private Clase clase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenido_id")
    private Contenido contenido;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRecomendacion estado;

    private LocalDateTime fechaGenerada;
    private String prioridad;
}

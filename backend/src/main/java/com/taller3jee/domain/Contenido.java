package com.taller3jee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "contenido")
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id", nullable = false)
    private Clase clase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContenido tipo;

    @Column(nullable = false)
    private String titulo;

    private String urlRecurso;

    @Column(columnDefinition = "TEXT")
    private String textoCuerpo;

    @Column(nullable = false)
    private Integer ordenEnClase;

    private Long tamanioBytes;
}

package com.taller3jee.dto;

import com.taller3jee.domain.TipoContenido;
import lombok.Data;

@Data
public class ContenidoDTO {
    private Long id;
    private Long claseId;
    private TipoContenido tipo;
    private String titulo;
    private String urlRecurso;
    private String textoCuerpo;
    private Integer ordenEnClase;
    private Long tamanioBytes;
}

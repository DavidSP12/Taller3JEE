package com.taller3jee.dto;

import lombok.Data;

import java.util.List;

@Data
public class RespuestasRequest {
    private List<RespuestaItem> respuestas;

    @Data
    public static class RespuestaItem {
        private Long preguntaId;
        private String respuesta;
    }
}

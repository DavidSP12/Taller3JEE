package com.taller3jee.dto;

import lombok.Data;

@Data
public class SimulacionRequest {
    private Long estudianteId;
    private String perfilAprendizaje; // RAPIDO, PROMEDIO, LENTO
    private double tasaError;
    private int clasesASimular;
    private long seed;
}

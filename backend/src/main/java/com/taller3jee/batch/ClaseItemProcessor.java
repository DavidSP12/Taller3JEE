package com.taller3jee.batch;

import com.taller3jee.domain.Clase;
import com.taller3jee.domain.Curso;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ClaseItemProcessor implements ItemProcessor<Map<String, Object>, Clase> {

    @Override
    public Clase process(Map<String, Object> item) {
        try {
            Clase clase = new Clase();
            clase.setNumero(((Number) item.get("numero")).intValue());
            clase.setTitulo((String) item.get("titulo"));
            clase.setDescripcion((String) item.getOrDefault("descripcion", ""));
            clase.setOrden(((Number) item.getOrDefault("orden", item.get("numero"))).intValue());
            Object duracion = item.get("duracionEstimadaMin");
            if (duracion != null) {
                clase.setDuracionEstimadaMin(((Number) duracion).intValue());
            }
            return clase;
        } catch (Exception e) {
            log.warn("Skipping invalid clase item: {}", e.getMessage());
            return null;
        }
    }
}

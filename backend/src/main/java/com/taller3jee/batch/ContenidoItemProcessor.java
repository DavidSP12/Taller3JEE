package com.taller3jee.batch;

import com.taller3jee.domain.Contenido;
import com.taller3jee.domain.TipoContenido;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ContenidoItemProcessor implements ItemProcessor<Map<String, Object>, Contenido> {

    @Value("${app.s3.endpoint:http://localhost:9000}")
    private String s3Endpoint;

    @Value("${app.s3.bucket:estructuras-datos}")
    private String s3Bucket;

    private static final java.util.Set<String> VALID_MIME_TYPES = java.util.Set.of(
            "TEXT", "PDF", "WORD", "EXCEL", "PPTX", "VIDEO", "IMAGE", "URL"
    );

    @Override
    public Contenido process(Map<String, Object> item) {
        try {
            String tipoStr = ((String) item.getOrDefault("tipo", "TEXT")).toUpperCase();
            if (!VALID_MIME_TYPES.contains(tipoStr)) {
                log.warn("Invalid content type '{}', defaulting to TEXT", tipoStr);
                tipoStr = "TEXT";
            }
            TipoContenido tipo = TipoContenido.valueOf(tipoStr);

            Contenido contenido = new Contenido();
            contenido.setTipo(tipo);
            contenido.setTitulo((String) item.get("titulo"));
            contenido.setOrdenEnClase(((Number) item.getOrDefault("ordenEnClase", 1)).intValue());

            String urlRecurso = (String) item.get("urlRecurso");
            if (urlRecurso == null || urlRecurso.isBlank()) {
                // Generate mock S3 URL
                String ext = extensionForType(tipo);
                urlRecurso = s3Endpoint + "/" + s3Bucket + "/contenidos/" + UUID.randomUUID() + ext;
            }
            contenido.setUrlRecurso(urlRecurso);

            if (item.containsKey("textoCuerpo")) {
                contenido.setTextoCuerpo((String) item.get("textoCuerpo"));
            }
            if (item.containsKey("tamanioBytes")) {
                contenido.setTamanioBytes(((Number) item.get("tamanioBytes")).longValue());
            }

            return contenido;
        } catch (Exception e) {
            log.warn("Skipping invalid contenido item: {}", e.getMessage());
            return null;
        }
    }

    private String extensionForType(TipoContenido tipo) {
        return switch (tipo) {
            case PDF -> ".pdf";
            case VIDEO -> ".mp4";
            case IMAGE -> ".png";
            case WORD -> ".docx";
            case EXCEL -> ".xlsx";
            case PPTX -> ".pptx";
            default -> ".txt";
        };
    }
}

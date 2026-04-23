package com.taller3jee.controller;

import com.taller3jee.domain.Clase;
import com.taller3jee.domain.Contenido;
import com.taller3jee.domain.Curso;
import com.taller3jee.dto.ClaseDTO;
import com.taller3jee.dto.ContenidoDTO;
import com.taller3jee.dto.CursoDTO;
import com.taller3jee.service.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @GetMapping("/api/v1/cursos/{id}")
    public ResponseEntity<CursoDTO> getCurso(@PathVariable Long id) {
        return ResponseEntity.ok(toCursoDTO(cursoService.getCurso(id)));
    }

    @GetMapping("/api/v1/cursos/{id}/clases")
    public ResponseEntity<List<ClaseDTO>> getClasesByCurso(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.getClasesByCurso(id).stream()
                .map(this::toClaseDTO).toList());
    }

    @GetMapping("/api/v1/clases/{id}")
    public ResponseEntity<ClaseDTO> getClase(@PathVariable Long id) {
        return ResponseEntity.ok(toClaseDTO(cursoService.getClase(id)));
    }

    @GetMapping("/api/v1/clases/{id}/contenidos")
    public ResponseEntity<List<ContenidoDTO>> getContenidos(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.getContenidos(id).stream()
                .map(this::toContenidoDTO).toList());
    }

    @GetMapping("/api/v1/contenidos/{id}/recurso")
    public ResponseEntity<Map<String, String>> getRecursoUrl(@PathVariable Long id) {
        String url = cursoService.getRecursoUrl(id);
        return ResponseEntity.ok(Map.of("url", url != null ? url : ""));
    }

    private CursoDTO toCursoDTO(Curso c) {
        CursoDTO dto = new CursoDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setDescripcion(c.getDescripcion());
        dto.setVersion(c.getVersion());
        dto.setEstado(c.getEstado());
        dto.setFechaCreacion(c.getFechaCreacion());
        return dto;
    }

    private ClaseDTO toClaseDTO(Clase c) {
        ClaseDTO dto = new ClaseDTO();
        dto.setId(c.getId());
        dto.setCursoId(c.getCurso().getId());
        dto.setNumero(c.getNumero());
        dto.setTitulo(c.getTitulo());
        dto.setDescripcion(c.getDescripcion());
        dto.setOrden(c.getOrden());
        dto.setDuracionEstimadaMin(c.getDuracionEstimadaMin());
        return dto;
    }

    private ContenidoDTO toContenidoDTO(Contenido c) {
        ContenidoDTO dto = new ContenidoDTO();
        dto.setId(c.getId());
        dto.setClaseId(c.getClase().getId());
        dto.setTipo(c.getTipo());
        dto.setTitulo(c.getTitulo());
        dto.setUrlRecurso(c.getUrlRecurso());
        dto.setTextoCuerpo(c.getTextoCuerpo());
        dto.setOrdenEnClase(c.getOrdenEnClase());
        dto.setTamanioBytes(c.getTamanioBytes());
        return dto;
    }
}

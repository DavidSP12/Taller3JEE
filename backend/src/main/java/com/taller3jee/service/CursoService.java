package com.taller3jee.service;

import com.taller3jee.domain.Clase;
import com.taller3jee.domain.Contenido;
import com.taller3jee.domain.Curso;
import com.taller3jee.repository.ClaseRepository;
import com.taller3jee.repository.ContenidoRepository;
import com.taller3jee.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CursoService {

    private final CursoRepository cursoRepository;
    private final ClaseRepository claseRepository;
    private final ContenidoRepository contenidoRepository;

    public List<Curso> getAllCursos() {
        return cursoRepository.findAll();
    }

    public Curso getCurso(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Curso not found: " + id));
    }

    public List<Clase> getClasesByCurso(Long cursoId) {
        cursoRepository.findById(cursoId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Curso not found: " + cursoId));
        return claseRepository.findByCursoIdOrderByNumero(cursoId);
    }

    public Clase getClase(Long id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Clase not found: " + id));
    }

    public List<Contenido> getContenidos(Long claseId) {
        claseRepository.findById(claseId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Clase not found: " + claseId));
        return contenidoRepository.findByClaseIdOrderByOrdenEnClase(claseId);
    }

    public String getRecursoUrl(Long contenidoId) {
        Contenido contenido = contenidoRepository.findById(contenidoId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Contenido not found: " + contenidoId));
        return contenido.getUrlRecurso();
    }
}

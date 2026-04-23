package com.taller3jee.repository;

import com.taller3jee.domain.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByEstudianteId(Long estudianteId);
    Optional<Inscripcion> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);
}

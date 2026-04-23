package com.taller3jee.repository;

import com.taller3jee.domain.RespuestaEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaEstudianteRepository extends JpaRepository<RespuestaEstudiante, Long> {
    List<RespuestaEstudiante> findByInscripcionIdAndEvaluacionIdAndIntento(Long inscripcionId, Long evaluacionId, Integer intento);
}

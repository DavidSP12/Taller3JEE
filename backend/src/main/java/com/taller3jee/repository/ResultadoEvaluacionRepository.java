package com.taller3jee.repository;

import com.taller3jee.domain.ResultadoEvaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoEvaluacionRepository extends JpaRepository<ResultadoEvaluacion, Long> {
    List<ResultadoEvaluacion> findByInscripcionIdAndEvaluacionId(Long inscripcionId, Long evaluacionId);
    long countByInscripcionIdAndEvaluacionIdAndPuntajeObtenidoLessThan(Long inscripcionId, Long evaluacionId, Double puntaje);
}

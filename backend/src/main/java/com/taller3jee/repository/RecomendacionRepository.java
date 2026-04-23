package com.taller3jee.repository;

import com.taller3jee.domain.EstadoRecomendacion;
import com.taller3jee.domain.Recomendacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {
    List<Recomendacion> findByInscripcionIdAndEstado(Long inscripcionId, EstadoRecomendacion estado);
    List<Recomendacion> findByInscripcionId(Long inscripcionId);
}

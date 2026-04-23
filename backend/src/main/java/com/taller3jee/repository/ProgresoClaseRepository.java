package com.taller3jee.repository;

import com.taller3jee.domain.EstadoProgreso;
import com.taller3jee.domain.ProgresoClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoClaseRepository extends JpaRepository<ProgresoClase, Long> {
    List<ProgresoClase> findByInscripcionId(Long inscripcionId);
    Optional<ProgresoClase> findByInscripcionIdAndClaseId(Long inscripcionId, Long claseId);
    long countByInscripcionIdAndEstado(Long inscripcionId, EstadoProgreso estado);
}

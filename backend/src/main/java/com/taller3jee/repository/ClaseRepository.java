package com.taller3jee.repository;

import com.taller3jee.domain.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    List<Clase> findByCursoId(Long cursoId);
    List<Clase> findByCursoIdOrderByNumero(Long cursoId);
}

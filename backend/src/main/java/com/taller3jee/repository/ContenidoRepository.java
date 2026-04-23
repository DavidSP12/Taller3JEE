package com.taller3jee.repository;

import com.taller3jee.domain.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenidoRepository extends JpaRepository<Contenido, Long> {
    List<Contenido> findByClaseId(Long claseId);
    List<Contenido> findByClaseIdOrderByOrdenEnClase(Long claseId);
}

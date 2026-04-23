package com.taller3jee.repository;

import com.taller3jee.domain.InteraccionContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteraccionContenidoRepository extends JpaRepository<InteraccionContenido, Long> {
    List<InteraccionContenido> findByInscripcionId(Long inscripcionId);
    Optional<InteraccionContenido> findByInscripcionIdAndContenidoId(Long inscripcionId, Long contenidoId);

    @Query("SELECT COALESCE(SUM(i.duracionSegundos), 0) FROM InteraccionContenido i " +
           "WHERE i.inscripcion.id = :inscripcionId AND i.contenido.clase.id = :claseId")
    Long sumDuracionByInscripcionIdAndClaseId(@Param("inscripcionId") Long inscripcionId,
                                               @Param("claseId") Long claseId);

    @Query("SELECT COALESCE(AVG(sub.total), 0) FROM " +
           "(SELECT SUM(i.duracionSegundos) as total FROM InteraccionContenido i " +
           "WHERE i.contenido.clase.id = :claseId GROUP BY i.inscripcion.id) sub")
    Double avgDuracionByClaseId(@Param("claseId") Long claseId);
}

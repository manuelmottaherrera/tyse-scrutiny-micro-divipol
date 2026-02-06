package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.Reclamacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository para la entidad Reclamacion.
 */
@Repository
public interface ReclamacionRepository extends R2dbcRepository<Reclamacion, Long> {
    Flux<Reclamacion> findByTestigoIdAndActivoTrue(Long testigoId);

    Flux<Reclamacion> findByMesaIdAndActivoTrue(Long mesaId);

    Flux<Reclamacion> findByComisionIdAndActivoTrue(Long comisionId);

    Flux<Reclamacion> findByEstadoAndActivoTrue(String estado);

    Flux<Reclamacion> findByTipoReclamacionAndActivoTrue(String tipoReclamacion);

    @Query(
        "SELECT * FROM reclamacion WHERE activo = true ORDER BY fecha_presentacion DESC LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}"
    )
    Flux<Reclamacion> findAllActive(Pageable pageable);

    Mono<Long> countByActivoTrue();

    Mono<Long> countByEstadoAndActivoTrue(String estado);

    Mono<Long> countByTestigoIdAndActivoTrue(Long testigoId);

    Mono<Long> countByMesaIdAndActivoTrue(Long mesaId);

    Mono<Long> countByComisionIdAndActivoTrue(Long comisionId);

    @Modifying
    @Query(
        "UPDATE reclamacion SET estado = :estado, resolucion = :resolucion, fecha_resolucion = CURRENT_TIMESTAMP, resuelta_por = :resueltaPor, last_modified_date = CURRENT_TIMESTAMP WHERE id = :id"
    )
    Mono<Integer> resolverReclamacion(
        @Param("id") Long id,
        @Param("estado") String estado,
        @Param("resolucion") String resolucion,
        @Param("resueltaPor") String resueltaPor
    );

    @Modifying
    @Query("UPDATE reclamacion SET estado = :estado, last_modified_date = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateEstadoById(@Param("id") Long id, @Param("estado") String estado);

    @Modifying
    @Query("UPDATE reclamacion SET activo = false, last_modified_date = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> deactivateById(@Param("id") Long id);
}

package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.Credencial;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository para la entidad Credencial.
 */
@Repository
public interface CredencialRepository extends R2dbcRepository<Credencial, Long> {
    Flux<Credencial> findByTestigoIdAndActivoTrue(Long testigoId);

    Flux<Credencial> findByEstadoAndActivoTrue(String estado);

    Flux<Credencial> findByTipoAndActivoTrue(String tipo);

    Mono<Credencial> findByCodigoVerificacionAndActivoTrue(String codigoVerificacion);

    Mono<Boolean> existsByTestigoMesaIdAndActivoTrue(Long testigoMesaId);

    Mono<Boolean> existsByTestigoComisionIdAndActivoTrue(Long testigoComisionId);

    Mono<Long> countByEstadoAndActivoTrue(String estado);

    Mono<Long> countByTestigoIdAndActivoTrue(Long testigoId);

    @Modifying
    @Query("UPDATE credencial SET estado = :estado, last_modified_date = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateEstadoById(@Param("id") Long id, @Param("estado") String estado);

    @Modifying
    @Query(
        "UPDATE credencial SET estado = :estado, fecha_emision = CURRENT_TIMESTAMP, emitido_por = :emitidoPor, last_modified_date = CURRENT_TIMESTAMP WHERE id = :id"
    )
    Mono<Integer> emitirCredencial(@Param("id") Long id, @Param("estado") String estado, @Param("emitidoPor") String emitidoPor);

    @Modifying
    @Query(
        "UPDATE credencial SET estado = :estado, fecha_entrega = CURRENT_TIMESTAMP, last_modified_date = CURRENT_TIMESTAMP WHERE id = :id"
    )
    Mono<Integer> entregarCredencial(@Param("id") Long id, @Param("estado") String estado);

    @Modifying
    @Query("UPDATE credencial SET activo = false, last_modified_date = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> deactivateById(@Param("id") Long id);
}

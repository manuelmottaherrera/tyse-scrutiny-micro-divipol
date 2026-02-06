package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.TestigoComision;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository para la entidad TestigoComision.
 */
@Repository
public interface TestigoComisionRepository extends R2dbcRepository<TestigoComision, Long> {
    Flux<TestigoComision> findByComisionIdAndActivoTrue(Long comisionId);

    Flux<TestigoComision> findByTestigoIdAndActivoTrue(Long testigoId);

    Mono<TestigoComision> findByTestigoIdAndComisionId(Long testigoId, Long comisionId);

    Mono<Boolean> existsByTestigoIdAndComisionIdAndActivoTrue(Long testigoId, Long comisionId);

    /**
     * Verifica si ya existe un testigo de la misma organización en la comisión.
     * Regla normativa: máximo 1 testigo por organización por comisión.
     */
    Mono<Boolean> existsByComisionIdAndOrganizacionIdAndActivoTrue(Long comisionId, Long organizacionId);

    Mono<Long> countByComisionIdAndActivoTrue(Long comisionId);

    @Modifying
    @Query(
        "UPDATE testigo_comision SET activo = false, last_modified_date = CURRENT_TIMESTAMP WHERE testigo_id = :testigoId AND comision_id = :comisionId"
    )
    Mono<Integer> deactivateByTestigoIdAndComisionId(@Param("testigoId") Long testigoId, @Param("comisionId") Long comisionId);
}

package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.TestigoMesa;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository para la entidad TestigoMesa.
 */
@Repository
public interface TestigoMesaRepository extends R2dbcRepository<TestigoMesa, Long> {
    Flux<TestigoMesa> findByMesaIdAndActivoTrue(Long mesaId);

    Flux<TestigoMesa> findByPuestoIdAndActivoTrue(Integer puestoId);

    Flux<TestigoMesa> findByTestigoIdAndActivoTrue(Long testigoId);

    Mono<TestigoMesa> findByTestigoIdAndMesaId(Long testigoId, Long mesaId);

    Mono<Boolean> existsByTestigoIdAndMesaIdAndActivoTrue(Long testigoId, Long mesaId);

    /**
     * Verifica si ya existe un testigo PRINCIPAL de la misma organización en la mesa.
     */
    Mono<Boolean> existsByMesaIdAndOrganizacionIdAndTipoTestigoAndActivoTrue(Long mesaId, Long organizacionId, String tipoTestigo);

    /**
     * Cuenta testigos de un tipo específico para una organización en un puesto.
     * Usado para validar límite de remanentes.
     */
    @Query(
        "SELECT COUNT(*) FROM testigo_mesa " +
        "WHERE puesto_id = :puestoId AND organizacion_id = :orgId " +
        "AND tipo_testigo = :tipoTestigo AND activo = true"
    )
    Mono<Long> countByPuestoIdAndOrganizacionIdAndTipoTestigoAndActivoTrue(
        @Param("puestoId") Integer puestoId,
        @Param("orgId") Long organizacionId,
        @Param("tipoTestigo") String tipoTestigo
    );

    Mono<Long> countByMesaIdAndActivoTrue(Long mesaId);

    Mono<Long> countByPuestoIdAndActivoTrue(Integer puestoId);

    @Modifying
    @Query(
        "UPDATE testigo_mesa SET activo = false, last_modified_date = CURRENT_TIMESTAMP WHERE testigo_id = :testigoId AND mesa_id = :mesaId"
    )
    Mono<Integer> deactivateByTestigoIdAndMesaId(@Param("testigoId") Long testigoId, @Param("mesaId") Long mesaId);
}

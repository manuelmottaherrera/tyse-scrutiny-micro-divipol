package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.TestigoPuesto;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para la entidad TestigoPuesto.
 * Gestiona la relación M:N entre testigos y puestos.
 */
@Repository
public interface TestigoPuestoRepository extends R2dbcRepository<TestigoPuesto, Long> {
    /**
     * Encuentra todas las asignaciones de testigos a un puesto.
     *
     * @param puestoId ID del puesto de votación
     * @return Flux con las asignaciones activas
     */
    Flux<TestigoPuesto> findByPuestoIdAndActivoTrue(Integer puestoId);

    /**
     * Encuentra todos los puestos asignados a un testigo.
     *
     * @param testigoId ID del testigo
     * @return Flux con las asignaciones activas
     */
    Flux<TestigoPuesto> findByTestigoIdAndActivoTrue(Long testigoId);

    /**
     * Busca una asignación específica testigo-puesto.
     *
     * @param testigoId ID del testigo
     * @param puestoId ID del puesto
     * @return Mono con la asignación o vacío
     */
    Mono<TestigoPuesto> findByTestigoIdAndPuestoId(Long testigoId, Integer puestoId);

    /**
     * Verifica si existe una asignación activa testigo-puesto.
     *
     * @param testigoId ID del testigo
     * @param puestoId ID del puesto
     * @return Mono true si existe
     */
    Mono<Boolean> existsByTestigoIdAndPuestoIdAndActivoTrue(Long testigoId, Integer puestoId);

    /**
     * Cuenta los testigos asignados a un puesto.
     *
     * @param puestoId ID del puesto
     * @return Mono con el conteo
     */
    Mono<Long> countByPuestoIdAndActivoTrue(Integer puestoId);

    /**
     * Cuenta los puestos asignados a un testigo.
     *
     * @param testigoId ID del testigo
     * @return Mono con el conteo
     */
    Mono<Long> countByTestigoIdAndActivoTrue(Long testigoId);

    /**
     * Desactiva una asignación testigo-puesto (soft delete).
     *
     * @param testigoId ID del testigo
     * @param puestoId ID del puesto
     * @return Mono con el número de filas afectadas
     */
    @Modifying
    @Query("UPDATE testigo_puesto SET activo = false WHERE testigo_id = :testigoId AND puesto_id = :puestoId")
    Mono<Integer> deactivateByTestigoIdAndPuestoId(Long testigoId, Integer puestoId);

    /**
     * Elimina físicamente una asignación testigo-puesto.
     *
     * @param testigoId ID del testigo
     * @param puestoId ID del puesto
     * @return Mono vacío cuando se completa
     */
    Mono<Void> deleteByTestigoIdAndPuestoId(Long testigoId, Integer puestoId);
}

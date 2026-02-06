package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.MesaVotacion;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para la entidad MesaVotacion.
 * Proporciona acceso reactivo a mesas de votación.
 */
@Repository
public interface MesaVotacionRepository extends R2dbcRepository<MesaVotacion, Long> {
    /**
     * Encuentra todas las mesas activas de un puesto.
     *
     * @param puestoId ID del puesto de votación
     * @return Flux con las mesas activas
     */
    Flux<MesaVotacion> findByPuestoIdAndActivoTrue(Integer puestoId);

    /**
     * Cuenta las mesas activas de un puesto.
     *
     * @param puestoId ID del puesto de votación
     * @return Mono con el conteo
     */
    Mono<Long> countByPuestoIdAndActivoTrue(Integer puestoId);

    /**
     * Busca una mesa específica por puesto y número.
     *
     * @param puestoId ID del puesto
     * @param numeroMesa Número de mesa
     * @return Mono con la mesa encontrada o vacío
     */
    Mono<MesaVotacion> findByPuestoIdAndNumeroMesa(Integer puestoId, Integer numeroMesa);

    /**
     * Encuentra todas las mesas activas.
     *
     * @return Flux con las mesas activas
     */
    Flux<MesaVotacion> findByActivoTrue();
}

package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.Jurado;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para la entidad Jurado.
 */
@Repository
public interface JuradoRepository extends R2dbcRepository<Jurado, Long> {
    /**
     * Encuentra todos los jurados asignados a un puesto de votación.
     *
     * @param puestoId ID del puesto de votación
     * @return Flux con los jurados del puesto
     */
    Flux<Jurado> findByPuestoId(Integer puestoId);

    /**
     * Cuenta los jurados asignados a un puesto.
     *
     * @param puestoId ID del puesto de votación
     * @return Mono con el conteo de jurados
     */
    Mono<Long> countByPuestoId(Integer puestoId);

    /**
     * Busca un jurado por tipo y número de documento.
     *
     * @param tipoDocumento Tipo de documento (CC, CE, etc.)
     * @param numeroDocumento Número de documento
     * @return Mono con el jurado encontrado o vacío
     */
    Mono<Jurado> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}

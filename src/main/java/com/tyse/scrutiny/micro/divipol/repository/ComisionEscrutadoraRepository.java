package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.ComisionEscrutadora;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para la entidad ComisionEscrutadora.
 * Proporciona acceso reactivo a comisiones escrutadoras.
 */
@Repository
public interface ComisionEscrutadoraRepository extends R2dbcRepository<ComisionEscrutadora, Long> {
    /**
     * Encuentra todas las comisiones activas.
     */
    Flux<ComisionEscrutadora> findByActivoTrue();

    /**
     * Encuentra comisiones activas por tipo.
     */
    Flux<ComisionEscrutadora> findByTipoAndActivoTrue(String tipo);

    /**
     * Cuenta las comisiones activas.
     */
    Mono<Long> countByActivoTrue();

    /**
     * Encuentra comisiones activas por departamento.
     */
    Flux<ComisionEscrutadora> findByDepartamentoIdAndActivoTrue(Integer departamentoId);

    /**
     * Encuentra comisiones activas por municipio.
     */
    Flux<ComisionEscrutadora> findByMunicipioIdAndActivoTrue(Integer municipioId);

    /**
     * Encuentra todas las comisiones activas con paginación.
     */
    @Query(
        "SELECT * FROM comision_escrutadora WHERE activo = true ORDER BY nombre LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}"
    )
    Flux<ComisionEscrutadora> findAllActive(Pageable pageable);

    /**
     * Busca comisiones por nombre o ubicación (parcial, case-insensitive).
     */
    @Query(
        """
        SELECT * FROM comision_escrutadora
        WHERE activo = true
        AND (LOWER(nombre) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(ubicacion) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY nombre
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """
    )
    Flux<ComisionEscrutadora> searchByNombreOrUbicacion(String query, Pageable pageable);

    /**
     * Cuenta comisiones que coinciden con la búsqueda.
     */
    @Query(
        """
        SELECT COUNT(*) FROM comision_escrutadora
        WHERE activo = true
        AND (LOWER(nombre) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(ubicacion) LIKE LOWER(CONCAT('%', :query, '%')))
        """
    )
    Mono<Long> countByNombreOrUbicacion(String query);
}

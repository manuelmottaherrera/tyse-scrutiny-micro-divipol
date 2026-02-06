package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.OrganizacionPolitica;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para la entidad OrganizacionPolitica.
 * Proporciona acceso reactivo al catálogo de organizaciones políticas.
 */
@Repository
public interface OrganizacionPoliticaRepository extends R2dbcRepository<OrganizacionPolitica, Long> {
    /**
     * Encuentra todas las organizaciones activas.
     */
    Flux<OrganizacionPolitica> findByActivoTrue();

    /**
     * Encuentra organizaciones activas por tipo.
     */
    Flux<OrganizacionPolitica> findByTipoAndActivoTrue(String tipo);

    /**
     * Busca una organización por nombre exacto.
     */
    Mono<OrganizacionPolitica> findByNombre(String nombre);

    /**
     * Verifica si existe una organización activa con ese nombre.
     */
    Mono<Boolean> existsByNombreAndActivoTrue(String nombre);

    /**
     * Cuenta las organizaciones activas.
     */
    Mono<Long> countByActivoTrue();

    /**
     * Encuentra todas las organizaciones activas con paginación.
     */
    @Query(
        "SELECT * FROM organizacion_politica WHERE activo = true ORDER BY nombre LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}"
    )
    Flux<OrganizacionPolitica> findAllActive(Pageable pageable);

    /**
     * Busca organizaciones por nombre o sigla (parcial, case-insensitive).
     */
    @Query(
        """
        SELECT * FROM organizacion_politica
        WHERE activo = true
        AND (LOWER(nombre) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(sigla) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY nombre
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """
    )
    Flux<OrganizacionPolitica> searchByNombreOrSigla(String query, Pageable pageable);

    /**
     * Cuenta organizaciones que coinciden con la búsqueda.
     */
    @Query(
        """
        SELECT COUNT(*) FROM organizacion_politica
        WHERE activo = true
        AND (LOWER(nombre) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(sigla) LIKE LOWER(CONCAT('%', :query, '%')))
        """
    )
    Mono<Long> countByNombreOrSigla(String query);
}

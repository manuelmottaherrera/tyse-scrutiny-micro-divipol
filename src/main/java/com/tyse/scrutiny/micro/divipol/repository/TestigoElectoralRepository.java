package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.TestigoElectoral;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para la entidad TestigoElectoral.
 */
@Repository
public interface TestigoElectoralRepository extends R2dbcRepository<TestigoElectoral, Long> {
    /**
     * Encuentra todos los testigos activos.
     *
     * @return Flux con los testigos activos
     */
    Flux<TestigoElectoral> findByActivoTrue();

    /**
     * Busca un testigo por tipo y número de documento.
     *
     * @param tipoDocumento Tipo de documento (CC, CE, etc.)
     * @param numeroDocumento Número de documento
     * @return Mono con el testigo encontrado o vacío
     */
    Mono<TestigoElectoral> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    /**
     * Busca testigos por nombre o apellido (búsqueda parcial, case-insensitive).
     *
     * @param query Término de búsqueda
     * @param pageable Paginación
     * @return Flux con los testigos encontrados
     */
    @Query(
        """
        SELECT * FROM testigo_electoral
        WHERE activo = true
        AND (LOWER(nombres) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(apellidos) LIKE LOWER(CONCAT('%', :query, '%'))
             OR numero_documento LIKE CONCAT('%', :query, '%'))
        ORDER BY apellidos, nombres
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """
    )
    Flux<TestigoElectoral> searchByNombreOrDocumento(String query, Pageable pageable);

    /**
     * Cuenta testigos que coinciden con la búsqueda.
     *
     * @param query Término de búsqueda
     * @return Mono con el conteo
     */
    @Query(
        """
        SELECT COUNT(*) FROM testigo_electoral
        WHERE activo = true
        AND (LOWER(nombres) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(apellidos) LIKE LOWER(CONCAT('%', :query, '%'))
             OR numero_documento LIKE CONCAT('%', :query, '%'))
        """
    )
    Mono<Long> countByNombreOrDocumento(String query);

    /**
     * Encuentra todos los testigos con paginación.
     *
     * @param pageable Paginación
     * @return Flux con los testigos
     */
    @Query(
        "SELECT * FROM testigo_electoral WHERE activo = true ORDER BY apellidos, nombres LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}"
    )
    Flux<TestigoElectoral> findAllActive(Pageable pageable);

    /**
     * Cuenta todos los testigos activos.
     *
     * @return Mono con el conteo
     */
    Mono<Long> countByActivoTrue();
}

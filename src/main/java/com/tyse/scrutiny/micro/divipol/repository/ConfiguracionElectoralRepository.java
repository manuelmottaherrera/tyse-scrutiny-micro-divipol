package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.ConfiguracionElectoral;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para la entidad ConfiguracionElectoral.
 * Gestiona parámetros clave-valor del proceso electoral.
 */
@Repository
public interface ConfiguracionElectoralRepository extends R2dbcRepository<ConfiguracionElectoral, Long> {
    /**
     * Encuentra una configuración por su clave única.
     *
     * @param clave Clave de configuración
     * @return Mono con la configuración encontrada o vacío
     */
    Mono<ConfiguracionElectoral> findByClaveAndActivoTrue(String clave);

    /**
     * Encuentra todas las configuraciones activas.
     *
     * @return Flux con las configuraciones activas
     */
    Flux<ConfiguracionElectoral> findByActivoTrue();

    /**
     * Verifica si existe una configuración con la clave dada.
     *
     * @param clave Clave a verificar
     * @return Mono true si existe
     */
    Mono<Boolean> existsByClaveAndActivoTrue(String clave);

    /**
     * Cuenta las configuraciones activas.
     *
     * @return Mono con el conteo
     */
    Mono<Long> countByActivoTrue();
}

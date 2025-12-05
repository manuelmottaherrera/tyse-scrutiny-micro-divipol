package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio personalizado para consultas a las vistas de Divipol
 */
public interface DivipolRepositoryCustom {
    /**
     * Obtiene todos los departamentos con información agregada
     */
    Flux<DivipolDepartamentoDTO> findAllDepartamentos();

    /**
     * Obtiene los municipios de un departamento específico
     */
    Flux<DivipolMunicipioDTO> findMunicipiosByDepartamento(Integer codDepto);

    /**
     * Obtiene las zonas de un municipio específico
     */
    Flux<DivipolZonaDTO> findZonasByMunicipio(Integer codDepto, Integer codMpio);

    /**
     * Obtiene los puestos de una zona específica
     */
    Flux<DivipolPuestoDTO> findPuestosByZona(Integer codDepto, Integer codMpio, Integer codZona);

    /**
     * Obtiene estadísticas generales (totales)
     */
    Mono<DivipolStatsDTO> getGeneralStats();

    /**
     * Obtiene estadísticas de un departamento específico
     */
    Mono<DivipolStatsDTO> getStatsByDepartamento(Integer codDepto);

    /**
     * Obtiene estadísticas de un municipio específico
     */
    Mono<DivipolStatsDTO> getStatsByMunicipio(Integer codDepto, Integer codMpio);

    /**
     * Obtiene estadísticas de una zona específica
     */
    Mono<DivipolStatsDTO> getStatsByZona(Integer codDepto, Integer codMpio, Integer codZona);

    /**
     * Busca registros por nombre usando full-text search
     */
    Flux<DivipolSearchResultDTO> searchByName(String query, int page, int size);

    /**
     * Cuenta los resultados de búsqueda por nombre
     */
    Mono<Long> countSearchByName(String query);

    /**
     * Busca registros por prefijo del código divipol compuesto
     */
    Flux<DivipolSearchResultDTO> searchByCode(String codePrefix, int page, int size);

    /**
     * Cuenta los resultados de búsqueda por código
     */
    Mono<Long> countSearchByCode(String codePrefix);

    /**
     * Obtiene sugerencias de autocompletado por nombre
     */
    Flux<DivipolSearchResultDTO> getSuggestionsByName(String query, int limit);

    /**
     * Obtiene sugerencias de autocompletado por código
     */
    Flux<DivipolSearchResultDTO> getSuggestionsByCode(String codePrefix, int limit);
}

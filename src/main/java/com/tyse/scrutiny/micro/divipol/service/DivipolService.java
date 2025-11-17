package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.repository.DivipolRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import com.tyse.scrutiny.micro.divipol.service.util.DivipolValidationUtil;
import com.tyse.scrutiny.micro.divipol.web.api.DivipolApiDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que implementa los endpoints de DIVIPOL
 */
@Service
public class DivipolService implements DivipolApiDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(DivipolService.class);

    private final DivipolRepository divipolRepository;

    public DivipolService(DivipolRepository divipolRepository) {
        this.divipolRepository = divipolRepository;
    }

    @Override
    public Mono<ResponseEntity<Flux<DivipolDepartamentoDTO>>> getAllDepartamentos(ServerWebExchange exchange) {
        LOG.debug("REST request to get all Departamentos");
        Flux<DivipolDepartamentoDTO> departamentos = divipolRepository.findAllDepartamentos();
        return Mono.just(ResponseEntity.ok(departamentos));
    }

    @Override
    public Mono<ResponseEntity<Flux<DivipolMunicipioDTO>>> getMunicipiosByDepartamento(Integer codDepto, ServerWebExchange exchange) {
        LOG.debug("REST request to get Municipios by departamento: {}", codDepto);
        return Mono.fromRunnable(() -> DivipolValidationUtil.validateCodDepto(codDepto)).then(
            Mono.fromCallable(() -> {
                Flux<DivipolMunicipioDTO> municipios = divipolRepository.findMunicipiosByDepartamento(codDepto);
                return ResponseEntity.ok(municipios);
            })
        );
    }

    @Override
    public Mono<ResponseEntity<Flux<DivipolZonaDTO>>> getZonasByMunicipio(Integer codDepto, Integer codMpio, ServerWebExchange exchange) {
        LOG.debug("REST request to get Zonas by municipio: {}/{}", codDepto, codMpio);
        return Mono.fromRunnable(() -> {
            DivipolValidationUtil.validateCodDepto(codDepto);
            DivipolValidationUtil.validateCodMunicipio(codMpio);
        }).then(
            Mono.fromCallable(() -> {
                Flux<DivipolZonaDTO> zonas = divipolRepository.findZonasByMunicipio(codDepto, codMpio);
                return ResponseEntity.ok(zonas);
            })
        );
    }

    @Override
    public Mono<ResponseEntity<Flux<DivipolPuestoDTO>>> getPuestosByZona(
        Integer codDepto,
        Integer codMpio,
        Integer codZona,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to get Puestos by zona: {}/{}/{}", codDepto, codMpio, codZona);
        return Mono.fromRunnable(() -> {
            DivipolValidationUtil.validateCodDepto(codDepto);
            DivipolValidationUtil.validateCodMunicipio(codMpio);
            DivipolValidationUtil.validateCodZona(codZona);
        }).then(
            Mono.fromCallable(() -> {
                Flux<DivipolPuestoDTO> puestos = divipolRepository.findPuestosByZona(codDepto, codMpio, codZona);
                return ResponseEntity.ok(puestos);
            })
        );
    }

    @Override
    public Mono<ResponseEntity<DivipolStatsDTO>> getGeneralStats(ServerWebExchange exchange) {
        LOG.debug("REST request to get general stats");
        return divipolRepository.getGeneralStats().map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<DivipolStatsDTO>> getStatsByDepartamento(Integer codDepto, ServerWebExchange exchange) {
        LOG.debug("REST request to get stats by departamento: {}", codDepto);
        return Mono.fromRunnable(() -> DivipolValidationUtil.validateCodDepto(codDepto)).then(
            divipolRepository.getStatsByDepartamento(codDepto).map(ResponseEntity::ok)
        );
    }

    @Override
    public Mono<ResponseEntity<DivipolStatsDTO>> getStatsByMunicipio(Integer codDepto, Integer codMpio, ServerWebExchange exchange) {
        LOG.debug("REST request to get stats by municipio: {}/{}", codDepto, codMpio);
        return Mono.fromRunnable(() -> {
            DivipolValidationUtil.validateCodDepto(codDepto);
            DivipolValidationUtil.validateCodMunicipio(codMpio);
        }).then(divipolRepository.getStatsByMunicipio(codDepto, codMpio).map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<DivipolStatsDTO>> getStatsByZona(
        Integer codDepto,
        Integer codMpio,
        Integer codZona,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to get stats by zona: {}/{}/{}", codDepto, codMpio, codZona);
        return Mono.fromRunnable(() -> {
            DivipolValidationUtil.validateCodDepto(codDepto);
            DivipolValidationUtil.validateCodMunicipio(codMpio);
            DivipolValidationUtil.validateCodZona(codZona);
        }).then(divipolRepository.getStatsByZona(codDepto, codMpio, codZona).map(ResponseEntity::ok));
    }
}

package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.repository.DivipolRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import com.tyse.scrutiny.micro.divipol.service.export.DivipolExportService;
import com.tyse.scrutiny.micro.divipol.service.util.DivipolValidationUtil;
import com.tyse.scrutiny.micro.divipol.web.api.DivipolApiDelegate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
    private final DivipolExportService exportService;
    private final DivipolPuestoService puestoService;

    public DivipolService(
        DivipolRepository divipolRepository,
        DivipolExportService exportService,
        @Lazy DivipolPuestoService puestoService
    ) {
        this.divipolRepository = divipolRepository;
        this.exportService = exportService;
        this.puestoService = puestoService;
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

    // =====================================================
    // Métodos de Búsqueda
    // =====================================================

    private static final String MODE_NAME = "name";
    private static final String MODE_CODE = "code";
    private static final int MIN_QUERY_LENGTH_NAME = 3;
    private static final int MAX_SUGGESTIONS = 5;

    @Override
    public Mono<ResponseEntity<DivipolSearchResultPage>> searchDivipol(
        String q,
        String mode,
        Integer page,
        Integer size,
        ServerWebExchange exchange
    ) {
        String searchMode = mode != null ? mode : MODE_NAME;
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        LOG.debug("REST request to search Divipol: q={}, mode={}, page={}, size={}", q, searchMode, pageNum, pageSize);

        // Validaciones
        if (q == null || q.trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El término de búsqueda es requerido"));
        }

        if (MODE_NAME.equals(searchMode) && q.trim().length() < MIN_QUERY_LENGTH_NAME) {
            return Mono.error(
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La búsqueda por nombre requiere al menos " + MIN_QUERY_LENGTH_NAME + " caracteres"
                )
            );
        }

        // Ejecutar búsqueda según el modo
        Flux<DivipolSearchResultDTO> results;
        Mono<Long> countMono;

        if (MODE_CODE.equals(searchMode)) {
            results = divipolRepository.searchByCode(q.trim(), pageNum, pageSize);
            countMono = divipolRepository.countSearchByCode(q.trim());
        } else {
            results = divipolRepository.searchByName(q.trim(), pageNum, pageSize);
            countMono = divipolRepository.countSearchByName(q.trim());
        }

        // Construir respuesta paginada
        return results
            .collectList()
            .zipWith(countMono)
            .map(tuple -> {
                List<DivipolSearchResultDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                DivipolSearchResultPage resultPage = new DivipolSearchResultPage()
                    .content(content)
                    .page(pageNum)
                    .size(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages);

                return ResponseEntity.ok(resultPage);
            });
    }

    @Override
    public Mono<ResponseEntity<Flux<DivipolSearchResultDTO>>> getSuggestions(String q, String mode, ServerWebExchange exchange) {
        String searchMode = mode != null ? mode : MODE_NAME;

        LOG.debug("REST request to get suggestions: q={}, mode={}", q, searchMode);

        // Validaciones
        if (q == null || q.trim().isEmpty()) {
            return Mono.just(ResponseEntity.ok(Flux.empty()));
        }

        if (MODE_NAME.equals(searchMode) && q.trim().length() < MIN_QUERY_LENGTH_NAME) {
            return Mono.just(ResponseEntity.ok(Flux.empty()));
        }

        // Obtener sugerencias según el modo
        Flux<DivipolSearchResultDTO> suggestions;

        if (MODE_CODE.equals(searchMode)) {
            suggestions = divipolRepository.getSuggestionsByCode(q.trim(), MAX_SUGGESTIONS);
        } else {
            suggestions = divipolRepository.getSuggestionsByName(q.trim(), MAX_SUGGESTIONS);
        }

        return Mono.just(ResponseEntity.ok(suggestions));
    }

    // =====================================================
    // Métodos de Exportación
    // =====================================================

    @Override
    public Mono<ResponseEntity<Resource>> exportFiltersToCsv(
        Integer codDepto,
        Integer codMpio,
        Integer codZona,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to export filters to CSV: depto={}, mpio={}, zona={}", codDepto, codMpio, codZona);

        return exportService
            .exportFiltersToCsv(codDepto, codMpio, codZona)
            .map(bytes -> buildFileResponse(bytes, "divipol-reporte.csv", "text/csv"));
    }

    @Override
    public Mono<ResponseEntity<Resource>> exportFiltersToPdf(
        Integer codDepto,
        Integer codMpio,
        Integer codZona,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to export filters to PDF: depto={}, mpio={}, zona={}", codDepto, codMpio, codZona);

        return exportService
            .exportFiltersToPdf(codDepto, codMpio, codZona)
            .map(bytes -> buildFileResponse(bytes, "divipol-reporte.pdf", "application/pdf"));
    }

    @Override
    public Mono<ResponseEntity<Resource>> exportSearchToCsv(
        String q,
        String mode,
        Integer page,
        Integer size,
        Boolean exportAll,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to export search to CSV: q={}, mode={}, exportAll={}", q, mode, exportAll);

        // Validaciones (igual que searchDivipol)
        if (q == null || q.trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El término de búsqueda es requerido"));
        }

        String searchMode = mode != null ? mode : MODE_NAME;
        if (MODE_NAME.equals(searchMode) && q.trim().length() < MIN_QUERY_LENGTH_NAME) {
            return Mono.error(
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La búsqueda por nombre requiere al menos " + MIN_QUERY_LENGTH_NAME + " caracteres"
                )
            );
        }

        return exportService
            .exportSearchToCsv(q, mode, page, size, exportAll)
            .map(bytes -> buildFileResponse(bytes, "divipol-busqueda.csv", "text/csv"));
    }

    @Override
    public Mono<ResponseEntity<Resource>> exportSearchToPdf(
        String q,
        String mode,
        Integer page,
        Integer size,
        Boolean exportAll,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to export search to PDF: q={}, mode={}, exportAll={}", q, mode, exportAll);

        // Validaciones (igual que searchDivipol)
        if (q == null || q.trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El término de búsqueda es requerido"));
        }

        String searchMode = mode != null ? mode : MODE_NAME;
        if (MODE_NAME.equals(searchMode) && q.trim().length() < MIN_QUERY_LENGTH_NAME) {
            return Mono.error(
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La búsqueda por nombre requiere al menos " + MIN_QUERY_LENGTH_NAME + " caracteres"
                )
            );
        }

        return exportService
            .exportSearchToPdf(q, mode, page, size, exportAll)
            .map(bytes -> buildFileResponse(bytes, "divipol-busqueda.pdf", "application/pdf"));
    }

    private ResponseEntity<Resource> buildFileResponse(byte[] bytes, String filename, String contentType) {
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentLength(bytes.length)
            .body(resource);
    }

    // =====================================================
    // Métodos de Detalle de Puesto (delegados a DivipolPuestoService)
    // =====================================================

    @Override
    public Mono<ResponseEntity<PuestoDetalleDTO>> getPuestoDetalle(Integer puestoId, ServerWebExchange exchange) {
        return puestoService.getPuestoDetalle(puestoId, exchange);
    }

    @Override
    public Mono<ResponseEntity<Flux<JuradoDTO>>> getJuradosByPuesto(Integer puestoId, ServerWebExchange exchange) {
        return puestoService.getJuradosByPuesto(puestoId, exchange);
    }

    @Override
    public Mono<ResponseEntity<Flux<TestigoAsignadoDTO>>> getTestigosByPuesto(Integer puestoId, ServerWebExchange exchange) {
        return puestoService.getTestigosByPuesto(puestoId, exchange);
    }

    @Override
    public Mono<ResponseEntity<TestigoAsignadoDTO>> asignarTestigoAPuesto(Integer puestoId, Long testigoId, ServerWebExchange exchange) {
        return puestoService.asignarTestigoAPuesto(puestoId, testigoId, exchange);
    }

    @Override
    public Mono<ResponseEntity<Void>> desasignarTestigoDePuesto(Integer puestoId, Long testigoId, ServerWebExchange exchange) {
        return puestoService.desasignarTestigoDePuesto(puestoId, testigoId, exchange);
    }
}

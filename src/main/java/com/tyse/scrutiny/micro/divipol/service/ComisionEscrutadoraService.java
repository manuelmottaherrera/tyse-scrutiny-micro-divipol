package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.domain.ComisionEscrutadora;
import com.tyse.scrutiny.micro.divipol.repository.ComisionEscrutadoraRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ComisionEscrutadoraCreateDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ComisionEscrutadoraDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ComisionEscrutadoraPage;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ComisionEscrutadoraUpdateDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoComisionAsignacionDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoComisionDTO;
import com.tyse.scrutiny.micro.divipol.web.api.ComisionesApiDelegate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que implementa el CRUD de Comisiones Escrutadoras.
 */
@Service
public class ComisionEscrutadoraService implements ComisionesApiDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ComisionEscrutadoraService.class);

    private final ComisionEscrutadoraRepository comisionRepository;
    private final TestigoAsignacionService testigoAsignacionService;

    public ComisionEscrutadoraService(
        ComisionEscrutadoraRepository comisionRepository,
        @Lazy TestigoAsignacionService testigoAsignacionService
    ) {
        this.comisionRepository = comisionRepository;
        this.testigoAsignacionService = testigoAsignacionService;
    }

    @Override
    public Mono<ResponseEntity<ComisionEscrutadoraPage>> getAllComisiones(
        String tipo,
        Integer departamentoId,
        Integer municipioId,
        Integer page,
        Integer size,
        ServerWebExchange exchange
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        LOG.debug(
            "REST request to get all Comisiones: tipo={}, depto={}, mpio={}, page={}, size={}",
            tipo,
            departamentoId,
            municipioId,
            pageNum,
            pageSize
        );

        PageRequest pageable = PageRequest.of(pageNum, pageSize);

        // Filtrar por tipo si se especifica
        if (tipo != null && !tipo.isBlank()) {
            return comisionRepository
                .findByTipoAndActivoTrue(tipo)
                .map(this::toDTO)
                .collectList()
                .map(content -> {
                    ComisionEscrutadoraPage resultPage = new ComisionEscrutadoraPage()
                        .content(content)
                        .page(pageNum)
                        .size(pageSize)
                        .totalElements((long) content.size())
                        .totalPages(1);
                    return ResponseEntity.ok(resultPage);
                });
        }

        // Filtrar por departamento
        if (departamentoId != null) {
            return comisionRepository
                .findByDepartamentoIdAndActivoTrue(departamentoId)
                .map(this::toDTO)
                .collectList()
                .map(content -> {
                    ComisionEscrutadoraPage resultPage = new ComisionEscrutadoraPage()
                        .content(content)
                        .page(pageNum)
                        .size(pageSize)
                        .totalElements((long) content.size())
                        .totalPages(1);
                    return ResponseEntity.ok(resultPage);
                });
        }

        // Filtrar por municipio
        if (municipioId != null) {
            return comisionRepository
                .findByMunicipioIdAndActivoTrue(municipioId)
                .map(this::toDTO)
                .collectList()
                .map(content -> {
                    ComisionEscrutadoraPage resultPage = new ComisionEscrutadoraPage()
                        .content(content)
                        .page(pageNum)
                        .size(pageSize)
                        .totalElements((long) content.size())
                        .totalPages(1);
                    return ResponseEntity.ok(resultPage);
                });
        }

        return comisionRepository
            .findAllActive(pageable)
            .map(this::toDTO)
            .collectList()
            .zipWith(comisionRepository.countByActivoTrue())
            .map(tuple -> {
                List<ComisionEscrutadoraDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                ComisionEscrutadoraPage resultPage = new ComisionEscrutadoraPage()
                    .content(content)
                    .page(pageNum)
                    .size(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages);

                return ResponseEntity.ok(resultPage);
            });
    }

    @Override
    public Mono<ResponseEntity<ComisionEscrutadoraDTO>> getComisionById(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to get Comision by id: {}", id);

        return comisionRepository
            .findById(id)
            .filter(ComisionEscrutadora::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comisión no encontrada")))
            .map(this::toDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<ComisionEscrutadoraDTO>> createComision(
        Mono<ComisionEscrutadoraCreateDTO> createDTO,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to create Comision");

        return createDTO
            .flatMap(dto -> {
                if (dto.getNombre() == null || dto.getNombre().isBlank()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre es requerido"));
                }
                if (dto.getTipo() == null) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo es requerido"));
                }

                ComisionEscrutadora comision = new ComisionEscrutadora();
                comision.setTipo(dto.getTipo().getValue());
                comision.setNombre(dto.getNombre());
                comision.setUbicacion(dto.getUbicacion());
                comision.setDepartamentoId(dto.getDepartamentoId());
                comision.setMunicipioId(dto.getMunicipioId());
                if (dto.getFechaInicio() != null) {
                    comision.setFechaInicio(dto.getFechaInicio());
                }
                comision.setActivo(true);
                comision.setCreatedDate(Instant.now());
                comision.setLastModifiedDate(Instant.now());

                return comisionRepository.save(comision);
            })
            .map(this::toDTO)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<ComisionEscrutadoraDTO>> updateComision(
        Long id,
        Mono<ComisionEscrutadoraUpdateDTO> updateDTO,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to update Comision: {}", id);

        return comisionRepository
            .findById(id)
            .filter(ComisionEscrutadora::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comisión no encontrada")))
            .flatMap(comision ->
                updateDTO.flatMap(dto -> {
                    if (dto.getTipo() != null) {
                        comision.setTipo(dto.getTipo().getValue());
                    }
                    if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
                        comision.setNombre(dto.getNombre());
                    }
                    if (dto.getUbicacion() != null) {
                        comision.setUbicacion(dto.getUbicacion());
                    }
                    if (dto.getDepartamentoId() != null) {
                        comision.setDepartamentoId(dto.getDepartamentoId());
                    }
                    if (dto.getMunicipioId() != null) {
                        comision.setMunicipioId(dto.getMunicipioId());
                    }
                    if (dto.getFechaInicio() != null) {
                        comision.setFechaInicio(dto.getFechaInicio());
                    }
                    comision.setLastModifiedDate(Instant.now());
                    return comisionRepository.save(comision);
                })
            )
            .map(this::toDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteComision(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to delete Comision: {}", id);

        return comisionRepository
            .findById(id)
            .filter(ComisionEscrutadora::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comisión no encontrada")))
            .flatMap(comision -> {
                comision.setActivo(false);
                comision.setLastModifiedDate(Instant.now());
                return comisionRepository.save(comision);
            })
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<ComisionEscrutadoraPage>> searchComisiones(
        String q,
        Integer page,
        Integer size,
        ServerWebExchange exchange
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        LOG.debug("REST request to search Comisiones: q={}, page={}, size={}", q, pageNum, pageSize);

        if (q == null || q.trim().length() < 2) {
            return Mono.error(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "El término de búsqueda debe tener al menos 2 caracteres")
            );
        }

        PageRequest pageable = PageRequest.of(pageNum, pageSize);

        return comisionRepository
            .searchByNombreOrUbicacion(q.trim(), pageable)
            .map(this::toDTO)
            .collectList()
            .zipWith(comisionRepository.countByNombreOrUbicacion(q.trim()))
            .map(tuple -> {
                List<ComisionEscrutadoraDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                ComisionEscrutadoraPage resultPage = new ComisionEscrutadoraPage()
                    .content(content)
                    .page(pageNum)
                    .size(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages);

                return ResponseEntity.ok(resultPage);
            });
    }

    // =====================================================
    // Mapper
    // =====================================================

    private ComisionEscrutadoraDTO toDTO(ComisionEscrutadora comision) {
        ComisionEscrutadoraDTO dto = new ComisionEscrutadoraDTO();
        dto.setId(comision.getId());
        dto.setTipo(ComisionEscrutadoraDTO.TipoEnum.fromValue(comision.getTipo()));
        dto.setNombre(comision.getNombre());
        dto.setUbicacion(comision.getUbicacion());
        dto.setDepartamentoId(comision.getDepartamentoId());
        dto.setMunicipioId(comision.getMunicipioId());
        dto.setFechaInicio(comision.getFechaInicio());
        dto.setActivo(comision.getActivo());
        return dto;
    }

    // =====================================================
    // Métodos de Testigos en Comisión
    // =====================================================

    @Override
    public Mono<ResponseEntity<Flux<TestigoComisionDTO>>> getTestigosByComision(Long comisionId, ServerWebExchange exchange) {
        LOG.debug("REST request to get Testigos by comision: {}", comisionId);
        Flux<TestigoComisionDTO> testigos = testigoAsignacionService.getTestigosByComision(comisionId);
        return Mono.just(ResponseEntity.ok(testigos));
    }

    @Override
    public Mono<ResponseEntity<TestigoComisionDTO>> asignarTestigoAComision(
        Long comisionId,
        Mono<TestigoComisionAsignacionDTO> dto,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to assign Testigo to comision: {}", comisionId);
        return dto
            .flatMap(d -> testigoAsignacionService.asignarTestigoAComision(comisionId, d, exchange))
            .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @Override
    public Mono<ResponseEntity<Void>> desasignarTestigoDeComision(Long comisionId, Long testigoId, ServerWebExchange exchange) {
        LOG.debug("REST request to unassign Testigo from comision: comision={}, testigo={}", comisionId, testigoId);
        return testigoAsignacionService
            .desasignarTestigoDeComision(comisionId, testigoId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}

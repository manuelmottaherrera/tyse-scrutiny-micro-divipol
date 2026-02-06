package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.domain.OrganizacionPolitica;
import com.tyse.scrutiny.micro.divipol.repository.OrganizacionPoliticaRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.OrganizacionPoliticaCreateDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.OrganizacionPoliticaDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.OrganizacionPoliticaPage;
import com.tyse.scrutiny.micro.divipol.service.api.dto.OrganizacionPoliticaUpdateDTO;
import com.tyse.scrutiny.micro.divipol.web.api.OrganizacionesApiDelegate;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Servicio que implementa el CRUD de Organizaciones Políticas.
 */
@Service
public class OrganizacionPoliticaService implements OrganizacionesApiDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizacionPoliticaService.class);

    private final OrganizacionPoliticaRepository organizacionRepository;

    public OrganizacionPoliticaService(OrganizacionPoliticaRepository organizacionRepository) {
        this.organizacionRepository = organizacionRepository;
    }

    @Override
    public Mono<ResponseEntity<OrganizacionPoliticaPage>> getAllOrganizaciones(
        String tipo,
        Integer page,
        Integer size,
        ServerWebExchange exchange
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        LOG.debug("REST request to get all Organizaciones: tipo={}, page={}, size={}", tipo, pageNum, pageSize);

        PageRequest pageable = PageRequest.of(pageNum, pageSize);

        if (tipo != null && !tipo.isBlank()) {
            return organizacionRepository
                .findByTipoAndActivoTrue(tipo)
                .map(this::toDTO)
                .collectList()
                .map(content -> {
                    OrganizacionPoliticaPage resultPage = new OrganizacionPoliticaPage()
                        .content(content)
                        .page(pageNum)
                        .size(pageSize)
                        .totalElements((long) content.size())
                        .totalPages(1);
                    return ResponseEntity.ok(resultPage);
                });
        }

        return organizacionRepository
            .findAllActive(pageable)
            .map(this::toDTO)
            .collectList()
            .zipWith(organizacionRepository.countByActivoTrue())
            .map(tuple -> {
                List<OrganizacionPoliticaDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                OrganizacionPoliticaPage resultPage = new OrganizacionPoliticaPage()
                    .content(content)
                    .page(pageNum)
                    .size(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages);

                return ResponseEntity.ok(resultPage);
            });
    }

    @Override
    public Mono<ResponseEntity<OrganizacionPoliticaDTO>> getOrganizacionById(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to get Organizacion by id: {}", id);

        return organizacionRepository
            .findById(id)
            .filter(OrganizacionPolitica::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Organización no encontrada")))
            .map(this::toDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<OrganizacionPoliticaDTO>> createOrganizacion(
        Mono<OrganizacionPoliticaCreateDTO> createDTO,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to create Organizacion");

        return createDTO
            .flatMap(dto -> {
                if (dto.getNombre() == null || dto.getNombre().isBlank()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre es requerido"));
                }
                if (dto.getTipo() == null) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo es requerido"));
                }

                return organizacionRepository
                    .findByNombre(dto.getNombre())
                    .flatMap(existing -> {
                        if (existing.getActivo()) {
                            return Mono.<OrganizacionPolitica>error(
                                new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una organización con ese nombre")
                            );
                        }
                        // Reactivar organización inactiva
                        existing.setActivo(true);
                        existing.setSigla(dto.getSigla());
                        existing.setTipo(dto.getTipo().getValue());
                        existing.setLastModifiedDate(Instant.now());
                        return organizacionRepository.save(existing);
                    })
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            OrganizacionPolitica org = new OrganizacionPolitica();
                            org.setNombre(dto.getNombre());
                            org.setSigla(dto.getSigla());
                            org.setTipo(dto.getTipo().getValue());
                            org.setActivo(true);
                            org.setCreatedDate(Instant.now());
                            org.setLastModifiedDate(Instant.now());
                            return organizacionRepository.save(org);
                        })
                    );
            })
            .map(this::toDTO)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<OrganizacionPoliticaDTO>> updateOrganizacion(
        Long id,
        Mono<OrganizacionPoliticaUpdateDTO> updateDTO,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to update Organizacion: {}", id);

        return organizacionRepository
            .findById(id)
            .filter(OrganizacionPolitica::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Organización no encontrada")))
            .flatMap(org ->
                updateDTO.flatMap(dto -> {
                    if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
                        org.setNombre(dto.getNombre());
                    }
                    if (dto.getSigla() != null) {
                        org.setSigla(dto.getSigla());
                    }
                    if (dto.getTipo() != null) {
                        org.setTipo(dto.getTipo().getValue());
                    }
                    org.setLastModifiedDate(Instant.now());
                    return organizacionRepository.save(org);
                })
            )
            .map(this::toDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteOrganizacion(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to delete Organizacion: {}", id);

        return organizacionRepository
            .findById(id)
            .filter(OrganizacionPolitica::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Organización no encontrada")))
            .flatMap(org -> {
                org.setActivo(false);
                org.setLastModifiedDate(Instant.now());
                return organizacionRepository.save(org);
            })
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<OrganizacionPoliticaPage>> searchOrganizaciones(
        String q,
        Integer page,
        Integer size,
        ServerWebExchange exchange
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        LOG.debug("REST request to search Organizaciones: q={}, page={}, size={}", q, pageNum, pageSize);

        if (q == null || q.trim().length() < 2) {
            return Mono.error(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "El término de búsqueda debe tener al menos 2 caracteres")
            );
        }

        PageRequest pageable = PageRequest.of(pageNum, pageSize);

        return organizacionRepository
            .searchByNombreOrSigla(q.trim(), pageable)
            .map(this::toDTO)
            .collectList()
            .zipWith(organizacionRepository.countByNombreOrSigla(q.trim()))
            .map(tuple -> {
                List<OrganizacionPoliticaDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                OrganizacionPoliticaPage resultPage = new OrganizacionPoliticaPage()
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

    private OrganizacionPoliticaDTO toDTO(OrganizacionPolitica org) {
        OrganizacionPoliticaDTO dto = new OrganizacionPoliticaDTO();
        dto.setId(org.getId());
        dto.setNombre(org.getNombre());
        dto.setSigla(org.getSigla());
        dto.setTipo(OrganizacionPoliticaDTO.TipoEnum.fromValue(org.getTipo()));
        dto.setActivo(org.getActivo());
        return dto;
    }
}

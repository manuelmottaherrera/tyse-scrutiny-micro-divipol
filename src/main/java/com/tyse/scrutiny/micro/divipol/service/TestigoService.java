package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.domain.TestigoElectoral;
import com.tyse.scrutiny.micro.divipol.repository.TestigoElectoralRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoPuestoRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoCreateDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoPage;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoUpdateDTO;
import com.tyse.scrutiny.micro.divipol.web.api.TestigosApiDelegate;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que implementa el CRUD de Testigos Electorales.
 */
@Service
public class TestigoService implements TestigosApiDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(TestigoService.class);

    private final TestigoElectoralRepository testigoRepository;
    private final TestigoPuestoRepository testigoPuestoRepository;

    public TestigoService(TestigoElectoralRepository testigoRepository, TestigoPuestoRepository testigoPuestoRepository) {
        this.testigoRepository = testigoRepository;
        this.testigoPuestoRepository = testigoPuestoRepository;
    }

    @Override
    public Mono<ResponseEntity<TestigoPage>> getAllTestigos(Integer page, Integer size, ServerWebExchange exchange) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        LOG.debug("REST request to get all Testigos: page={}, size={}", pageNum, pageSize);

        PageRequest pageable = PageRequest.of(pageNum, pageSize);

        return testigoRepository
            .findAllActive(pageable)
            .flatMap(this::toTestigoDTO)
            .collectList()
            .zipWith(testigoRepository.countByActivoTrue())
            .map(tuple -> {
                List<TestigoDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                TestigoPage resultPage = new TestigoPage()
                    .content(content)
                    .page(pageNum)
                    .size(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages);

                return ResponseEntity.ok(resultPage);
            });
    }

    @Override
    public Mono<ResponseEntity<TestigoDTO>> getTestigoById(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to get Testigo by id: {}", id);

        return testigoRepository
            .findById(id)
            .filter(TestigoElectoral::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Testigo no encontrado")))
            .flatMap(this::toTestigoDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<TestigoDTO>> createTestigo(Mono<TestigoCreateDTO> testigoCreateDTO, ServerWebExchange exchange) {
        LOG.debug("REST request to create Testigo");

        String currentUser = extractCurrentUser(exchange);

        return testigoCreateDTO
            .flatMap(dto -> {
                // Validar campos requeridos
                if (dto.getTipoDocumento() == null || dto.getTipoDocumento().isBlank()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de documento es requerido"));
                }
                if (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().isBlank()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Número de documento es requerido"));
                }
                if (dto.getNombres() == null || dto.getNombres().isBlank()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombres son requeridos"));
                }
                if (dto.getApellidos() == null || dto.getApellidos().isBlank()) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apellidos son requeridos"));
                }

                // Verificar que no exista un testigo con el mismo documento
                return testigoRepository
                    .findByTipoDocumentoAndNumeroDocumento(dto.getTipoDocumento(), dto.getNumeroDocumento())
                    .flatMap(existing -> {
                        if (existing.getActivo()) {
                            return Mono.<TestigoElectoral>error(
                                new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un testigo con ese documento")
                            );
                        }
                        // Reactivar testigo inactivo
                        existing.setActivo(true);
                        existing.setNombres(dto.getNombres());
                        existing.setApellidos(dto.getApellidos());
                        existing.setTelefono(dto.getTelefono());
                        existing.setEmail(dto.getEmail());
                        existing.setLastModifiedDate(Instant.now());
                        existing.setLastModifiedBy(currentUser);
                        return testigoRepository.save(existing);
                    })
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            // Crear nuevo testigo
                            TestigoElectoral testigo = new TestigoElectoral();
                            testigo.setTipoDocumento(dto.getTipoDocumento());
                            testigo.setNumeroDocumento(dto.getNumeroDocumento());
                            testigo.setNombres(dto.getNombres());
                            testigo.setApellidos(dto.getApellidos());
                            testigo.setTelefono(dto.getTelefono());
                            testigo.setEmail(dto.getEmail());
                            testigo.setActivo(true);
                            testigo.setCreatedDate(Instant.now());
                            testigo.setCreatedBy(currentUser);
                            testigo.setLastModifiedDate(Instant.now());
                            testigo.setLastModifiedBy(currentUser);
                            return testigoRepository.save(testigo);
                        })
                    );
            })
            .flatMap(this::toTestigoDTO)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<TestigoDTO>> updateTestigo(Long id, Mono<TestigoUpdateDTO> testigoUpdateDTO, ServerWebExchange exchange) {
        LOG.debug("REST request to update Testigo: {}", id);

        String currentUser = extractCurrentUser(exchange);

        return testigoRepository
            .findById(id)
            .filter(TestigoElectoral::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Testigo no encontrado")))
            .flatMap(testigo ->
                testigoUpdateDTO.flatMap(dto -> {
                    if (dto.getNombres() != null && !dto.getNombres().isBlank()) {
                        testigo.setNombres(dto.getNombres());
                    }
                    if (dto.getApellidos() != null && !dto.getApellidos().isBlank()) {
                        testigo.setApellidos(dto.getApellidos());
                    }
                    testigo.setTelefono(dto.getTelefono());
                    testigo.setEmail(dto.getEmail());
                    testigo.setLastModifiedDate(Instant.now());
                    testigo.setLastModifiedBy(currentUser);
                    return testigoRepository.save(testigo);
                })
            )
            .flatMap(this::toTestigoDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteTestigo(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to delete Testigo: {}", id);

        String currentUser = extractCurrentUser(exchange);

        return testigoRepository
            .findById(id)
            .filter(TestigoElectoral::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Testigo no encontrado")))
            .flatMap(testigo -> {
                testigo.setActivo(false);
                testigo.setLastModifiedDate(Instant.now());
                testigo.setLastModifiedBy(currentUser);
                return testigoRepository.save(testigo);
            })
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<TestigoPage>> searchTestigos(String q, Integer page, Integer size, ServerWebExchange exchange) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        LOG.debug("REST request to search Testigos: q={}, page={}, size={}", q, pageNum, pageSize);

        if (q == null || q.trim().length() < 2) {
            return Mono.error(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "El término de búsqueda debe tener al menos 2 caracteres")
            );
        }

        PageRequest pageable = PageRequest.of(pageNum, pageSize);

        return testigoRepository
            .searchByNombreOrDocumento(q.trim(), pageable)
            .flatMap(this::toTestigoDTO)
            .collectList()
            .zipWith(testigoRepository.countByNombreOrDocumento(q.trim()))
            .map(tuple -> {
                List<TestigoDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                TestigoPage resultPage = new TestigoPage()
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

    private Mono<TestigoDTO> toTestigoDTO(TestigoElectoral testigo) {
        return testigoPuestoRepository
            .countByTestigoIdAndActivoTrue(testigo.getId())
            .map(puestosCount -> {
                TestigoDTO dto = new TestigoDTO();
                dto.setId(testigo.getId());
                dto.setTipoDocumento(testigo.getTipoDocumento());
                dto.setNumeroDocumento(testigo.getNumeroDocumento());
                dto.setNombres(testigo.getNombres());
                dto.setApellidos(testigo.getApellidos());
                dto.setTelefono(testigo.getTelefono());
                dto.setEmail(testigo.getEmail());
                dto.setActivo(testigo.getActivo());
                dto.setPuestosAsignados(puestosCount.intValue());
                return dto;
            });
    }

    private String extractCurrentUser(ServerWebExchange exchange) {
        try {
            return exchange.getPrincipal().map(principal -> principal.getName()).defaultIfEmpty("system").block();
        } catch (Exception e) {
            return "system";
        }
    }
}

package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.domain.ComisionEscrutadora;
import com.tyse.scrutiny.micro.divipol.domain.Credencial;
import com.tyse.scrutiny.micro.divipol.domain.MesaVotacion;
import com.tyse.scrutiny.micro.divipol.domain.TestigoComision;
import com.tyse.scrutiny.micro.divipol.domain.TestigoElectoral;
import com.tyse.scrutiny.micro.divipol.domain.TestigoMesa;
import com.tyse.scrutiny.micro.divipol.repository.ComisionEscrutadoraRepository;
import com.tyse.scrutiny.micro.divipol.repository.CredencialRepository;
import com.tyse.scrutiny.micro.divipol.repository.MesaVotacionRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoComisionRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoElectoralRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoMesaRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.CredencialCreateDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.CredencialDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.CredencialEstadoDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.CredencialPage;
import com.tyse.scrutiny.micro.divipol.web.api.CredencialesApiDelegate;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que implementa el CRUD de Credenciales Electorales.
 *
 * Tipos de credencial:
 * - E15: Credencial para testigo de mesa (Art. 108 Código Electoral)
 * - E16: Credencial para testigo de comisión escrutadora (Art. 163)
 *
 * Estados y transiciones:
 * - PENDIENTE → EMITIDA (requiere emitidoPor)
 * - EMITIDA → ENTREGADA
 * - Cualquiera → ANULADA
 */
@Service
public class CredencialService implements CredencialesApiDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(CredencialService.class);

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_EMITIDA = "EMITIDA";
    private static final String ESTADO_ENTREGADA = "ENTREGADA";
    private static final String ESTADO_ANULADA = "ANULADA";

    private static final String TIPO_E15 = "E15";
    private static final String TIPO_E16 = "E16";

    private final CredencialRepository credencialRepository;
    private final TestigoElectoralRepository testigoRepository;
    private final TestigoMesaRepository testigoMesaRepository;
    private final TestigoComisionRepository testigoComisionRepository;
    private final MesaVotacionRepository mesaVotacionRepository;
    private final ComisionEscrutadoraRepository comisionRepository;

    public CredencialService(
        CredencialRepository credencialRepository,
        TestigoElectoralRepository testigoRepository,
        TestigoMesaRepository testigoMesaRepository,
        TestigoComisionRepository testigoComisionRepository,
        MesaVotacionRepository mesaVotacionRepository,
        ComisionEscrutadoraRepository comisionRepository
    ) {
        this.credencialRepository = credencialRepository;
        this.testigoRepository = testigoRepository;
        this.testigoMesaRepository = testigoMesaRepository;
        this.testigoComisionRepository = testigoComisionRepository;
        this.mesaVotacionRepository = mesaVotacionRepository;
        this.comisionRepository = comisionRepository;
    }

    @Override
    public Mono<ResponseEntity<CredencialPage>> getAllCredenciales(
        String estado,
        String tipo,
        Integer page,
        Integer size,
        ServerWebExchange exchange
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        LOG.debug("REST request to get all Credenciales: estado={}, tipo={}, page={}, size={}", estado, tipo, pageNum, pageSize);

        Flux<Credencial> credenciales;
        Mono<Long> countMono;

        if (estado != null && !estado.isBlank()) {
            credenciales = credencialRepository.findByEstadoAndActivoTrue(estado);
            countMono = credencialRepository.countByEstadoAndActivoTrue(estado);
        } else if (tipo != null && !tipo.isBlank()) {
            credenciales = credencialRepository.findByTipoAndActivoTrue(tipo);
            countMono = credenciales.count();
        } else {
            credenciales = credencialRepository.findAll().filter(c -> c.getActivo());
            countMono = credenciales.count();
        }

        return credenciales
            .skip((long) pageNum * pageSize)
            .take(pageSize)
            .flatMap(this::enrichCredencialDTO)
            .collectList()
            .zipWith(countMono)
            .map(tuple -> {
                List<CredencialDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                CredencialPage resultPage = new CredencialPage()
                    .content(content)
                    .page(pageNum)
                    .size(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages);

                return ResponseEntity.ok(resultPage);
            });
    }

    @Override
    public Mono<ResponseEntity<CredencialDTO>> getCredencialById(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to get Credencial by id: {}", id);

        return credencialRepository
            .findById(id)
            .filter(Credencial::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Credencial no encontrada")))
            .flatMap(this::enrichCredencialDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CredencialDTO>> createCredencial(Mono<CredencialCreateDTO> createDTO, ServerWebExchange exchange) {
        LOG.debug("REST request to create Credencial");

        return createDTO
            .flatMap(dto -> {
                // Validar tipo
                String tipo = dto.getTipo().getValue();
                if (!TIPO_E15.equals(tipo) && !TIPO_E16.equals(tipo)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de credencial inválido"));
                }

                // Validar asignación según tipo
                if (TIPO_E15.equals(tipo) && dto.getTestigoMesaId() == null) {
                    return Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "testigoMesaId es requerido para credencial E15")
                    );
                }
                if (TIPO_E16.equals(tipo) && dto.getTestigoComisionId() == null) {
                    return Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "testigoComisionId es requerido para credencial E16")
                    );
                }

                // Verificar que el testigo existe
                return testigoRepository
                    .findById(dto.getTestigoId())
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Testigo no encontrado")))
                    .flatMap(testigo -> {
                        // Verificar que no existe ya una credencial activa para esta asignación
                        Mono<Boolean> existsCheck;
                        if (TIPO_E15.equals(tipo)) {
                            existsCheck = credencialRepository.existsByTestigoMesaIdAndActivoTrue(dto.getTestigoMesaId());
                        } else {
                            existsCheck = credencialRepository.existsByTestigoComisionIdAndActivoTrue(dto.getTestigoComisionId());
                        }

                        return existsCheck.flatMap(exists -> {
                            if (exists) {
                                return Mono.error(
                                    new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una credencial activa para esta asignación")
                                );
                            }

                            return extractCurrentUser(exchange).flatMap(currentUser -> {
                                Credencial credencial = new Credencial();
                                credencial.setTestigoId(dto.getTestigoId());
                                credencial.setTipo(tipo);
                                credencial.setEstado(ESTADO_PENDIENTE);
                                credencial.setCodigoVerificacion(generateCodigoVerificacion());
                                credencial.setTestigoMesaId(dto.getTestigoMesaId());
                                credencial.setTestigoComisionId(dto.getTestigoComisionId());
                                credencial.setActivo(true);
                                credencial.setCreatedDate(Instant.now());
                                credencial.setLastModifiedDate(Instant.now());
                                credencial.setCreatedBy(currentUser);

                                return credencialRepository.save(credencial);
                            });
                        });
                    });
            })
            .flatMap(this::enrichCredencialDTO)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<CredencialDTO>> updateCredencialEstado(
        Long id,
        Mono<CredencialEstadoDTO> estadoDTO,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to update Credencial estado: {}", id);

        return credencialRepository
            .findById(id)
            .filter(Credencial::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Credencial no encontrada")))
            .flatMap(credencial ->
                estadoDTO.flatMap(dto -> {
                    String nuevoEstado = dto.getEstado().getValue();
                    String estadoActual = credencial.getEstado();

                    // Validar transiciones
                    if (!isValidTransition(estadoActual, nuevoEstado)) {
                        return Mono.error(
                            new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                String.format("Transición de estado inválida: %s → %s", estadoActual, nuevoEstado)
                            )
                        );
                    }

                    return extractCurrentUser(exchange).flatMap(currentUser -> {
                        credencial.setEstado(nuevoEstado);
                        credencial.setLastModifiedDate(Instant.now());

                        if (ESTADO_EMITIDA.equals(nuevoEstado)) {
                            credencial.setFechaEmision(Instant.now());
                            credencial.setEmitidoPor(currentUser);
                        } else if (ESTADO_ENTREGADA.equals(nuevoEstado)) {
                            credencial.setFechaEntrega(Instant.now());
                        }

                        return credencialRepository.save(credencial);
                    });
                })
            )
            .flatMap(this::enrichCredencialDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> anularCredencial(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to anular Credencial: {}", id);

        return credencialRepository
            .findById(id)
            .filter(Credencial::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Credencial no encontrada")))
            .flatMap(credencial -> {
                credencial.setEstado(ESTADO_ANULADA);
                credencial.setActivo(false);
                credencial.setLastModifiedDate(Instant.now());
                return credencialRepository.save(credencial);
            })
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<CredencialDTO>>> getCredencialesByTestigo(Long testigoId, ServerWebExchange exchange) {
        LOG.debug("REST request to get Credenciales by testigo: {}", testigoId);

        Flux<CredencialDTO> credenciales = credencialRepository.findByTestigoIdAndActivoTrue(testigoId).flatMap(this::enrichCredencialDTO);

        return Mono.just(ResponseEntity.ok(credenciales));
    }

    @Override
    public Mono<ResponseEntity<CredencialDTO>> verificarCredencial(String codigo, ServerWebExchange exchange) {
        LOG.debug("REST request to verify Credencial by codigo: {}", codigo);

        return credencialRepository
            .findByCodigoVerificacionAndActivoTrue(codigo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Credencial no encontrada o inválida")))
            .flatMap(this::enrichCredencialDTO)
            .map(ResponseEntity::ok);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private boolean isValidTransition(String from, String to) {
        // Cualquier estado puede ir a ANULADA
        if (ESTADO_ANULADA.equals(to)) {
            return true;
        }

        // PENDIENTE → EMITIDA
        if (ESTADO_PENDIENTE.equals(from) && ESTADO_EMITIDA.equals(to)) {
            return true;
        }

        // EMITIDA → ENTREGADA
        if (ESTADO_EMITIDA.equals(from) && ESTADO_ENTREGADA.equals(to)) {
            return true;
        }

        return false;
    }

    private String generateCodigoVerificacion() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private Mono<CredencialDTO> enrichCredencialDTO(Credencial credencial) {
        Mono<TestigoElectoral> testigoMono = testigoRepository.findById(credencial.getTestigoId()).defaultIfEmpty(new TestigoElectoral());

        Mono<String> asignacionDescMono;
        if (credencial.getTestigoMesaId() != null) {
            asignacionDescMono = testigoMesaRepository
                .findById(credencial.getTestigoMesaId())
                .flatMap(tm ->
                    mesaVotacionRepository
                        .findById(tm.getMesaId())
                        .map(mesa -> "Mesa " + mesa.getNumeroMesa() + " - Puesto " + mesa.getPuestoId())
                )
                .defaultIfEmpty("Mesa desconocida");
        } else if (credencial.getTestigoComisionId() != null) {
            asignacionDescMono = testigoComisionRepository
                .findById(credencial.getTestigoComisionId())
                .flatMap(tc -> comisionRepository.findById(tc.getComisionId()).map(ComisionEscrutadora::getNombre))
                .defaultIfEmpty("Comisión desconocida");
        } else {
            asignacionDescMono = Mono.just("");
        }

        return Mono.zip(testigoMono, asignacionDescMono).map(tuple -> {
            TestigoElectoral testigo = tuple.getT1();
            String asignacionDesc = tuple.getT2();

            CredencialDTO dto = new CredencialDTO();
            dto.setId(credencial.getId());
            dto.setTestigoId(credencial.getTestigoId());
            dto.setTestigoNombreCompleto(testigo.getNombres() + " " + testigo.getApellidos());
            dto.setTestigoNumeroDocumento(testigo.getNumeroDocumento());
            dto.setTipo(CredencialDTO.TipoEnum.fromValue(credencial.getTipo()));
            dto.setEstado(CredencialDTO.EstadoEnum.fromValue(credencial.getEstado()));
            dto.setCodigoVerificacion(credencial.getCodigoVerificacion());
            dto.setTestigoMesaId(credencial.getTestigoMesaId());
            dto.setTestigoComisionId(credencial.getTestigoComisionId());
            dto.setAsignacionDescripcion(asignacionDesc);
            if (credencial.getFechaEmision() != null) {
                dto.setFechaEmision(OffsetDateTime.ofInstant(credencial.getFechaEmision(), ZoneOffset.UTC));
            }
            if (credencial.getFechaEntrega() != null) {
                dto.setFechaEntrega(OffsetDateTime.ofInstant(credencial.getFechaEntrega(), ZoneOffset.UTC));
            }
            dto.setEmitidoPor(credencial.getEmitidoPor());
            dto.setActivo(credencial.getActivo());
            return dto;
        });
    }

    private Mono<String> extractCurrentUser(ServerWebExchange exchange) {
        return exchange.getPrincipal().map(principal -> principal.getName()).defaultIfEmpty("system");
    }
}

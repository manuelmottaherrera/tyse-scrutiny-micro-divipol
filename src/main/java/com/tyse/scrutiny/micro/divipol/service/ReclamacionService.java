package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.domain.ComisionEscrutadora;
import com.tyse.scrutiny.micro.divipol.domain.MesaVotacion;
import com.tyse.scrutiny.micro.divipol.domain.Reclamacion;
import com.tyse.scrutiny.micro.divipol.domain.TestigoElectoral;
import com.tyse.scrutiny.micro.divipol.repository.ComisionEscrutadoraRepository;
import com.tyse.scrutiny.micro.divipol.repository.MesaVotacionRepository;
import com.tyse.scrutiny.micro.divipol.repository.ReclamacionRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoComisionRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoElectoralRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoMesaRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ReclamacionCreateDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ReclamacionDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ReclamacionPage;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ReclamacionResolucionDTO;
import com.tyse.scrutiny.micro.divipol.web.api.ReclamacionesApiDelegate;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
 * Servicio que implementa el CRUD de Reclamaciones Electorales.
 *
 * Tipos de reclamación (Art. 165-168 Código Electoral):
 * - IRREGULARIDAD_MESA: Irregularidades en el proceso de votación
 * - EXCESO_VOTANTES: Votos superiores al potencial electoral
 * - ERROR_ARITMETICO: Errores en sumas o cómputos
 * - ERROR_NOMBRES: Discrepancias en nombres de candidatos
 * - FIRMAS_INSUFICIENTES: Falta de firmas requeridas en actas
 * - DISCREPANCIA_ACTAS: Diferencias entre actas E14/E24/E26
 * - OTRO: Otras causales de reclamación
 *
 * Estados:
 * - PRESENTADA: Reclamación registrada
 * - EN_REVISION: En proceso de análisis
 * - ACEPTADA: Reclamación aceptada y aplicada
 * - RECHAZADA: Reclamación desestimada
 */
@Service
public class ReclamacionService implements ReclamacionesApiDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ReclamacionService.class);

    private static final String ESTADO_PRESENTADA = "PRESENTADA";
    private static final String ESTADO_EN_REVISION = "EN_REVISION";
    private static final String ESTADO_ACEPTADA = "ACEPTADA";
    private static final String ESTADO_RECHAZADA = "RECHAZADA";

    private final ReclamacionRepository reclamacionRepository;
    private final TestigoElectoralRepository testigoRepository;
    private final TestigoMesaRepository testigoMesaRepository;
    private final TestigoComisionRepository testigoComisionRepository;
    private final MesaVotacionRepository mesaVotacionRepository;
    private final ComisionEscrutadoraRepository comisionRepository;

    public ReclamacionService(
        ReclamacionRepository reclamacionRepository,
        TestigoElectoralRepository testigoRepository,
        TestigoMesaRepository testigoMesaRepository,
        TestigoComisionRepository testigoComisionRepository,
        MesaVotacionRepository mesaVotacionRepository,
        ComisionEscrutadoraRepository comisionRepository
    ) {
        this.reclamacionRepository = reclamacionRepository;
        this.testigoRepository = testigoRepository;
        this.testigoMesaRepository = testigoMesaRepository;
        this.testigoComisionRepository = testigoComisionRepository;
        this.mesaVotacionRepository = mesaVotacionRepository;
        this.comisionRepository = comisionRepository;
    }

    @Override
    public Mono<ResponseEntity<ReclamacionPage>> getAllReclamaciones(
        String estado,
        String tipo,
        Integer page,
        Integer size,
        ServerWebExchange exchange
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        LOG.debug("REST request to get all Reclamaciones: estado={}, tipo={}, page={}, size={}", estado, tipo, pageNum, pageSize);

        PageRequest pageable = PageRequest.of(pageNum, pageSize);

        Flux<Reclamacion> reclamaciones;
        Mono<Long> countMono;

        if (estado != null && !estado.isBlank()) {
            reclamaciones = reclamacionRepository.findByEstadoAndActivoTrue(estado);
            countMono = reclamacionRepository.countByEstadoAndActivoTrue(estado);
        } else if (tipo != null && !tipo.isBlank()) {
            reclamaciones = reclamacionRepository.findByTipoReclamacionAndActivoTrue(tipo);
            countMono = reclamaciones.count();
        } else {
            reclamaciones = reclamacionRepository.findAllActive(pageable);
            countMono = reclamacionRepository.countByActivoTrue();
        }

        return reclamaciones
            .flatMap(this::enrichReclamacionDTO)
            .collectList()
            .zipWith(countMono)
            .map(tuple -> {
                List<ReclamacionDTO> content = tuple.getT1();
                Long totalElements = tuple.getT2();
                int totalPages = (int) Math.ceil((double) totalElements / pageSize);

                ReclamacionPage resultPage = new ReclamacionPage()
                    .content(content)
                    .page(pageNum)
                    .size(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages);

                return ResponseEntity.ok(resultPage);
            });
    }

    @Override
    public Mono<ResponseEntity<ReclamacionDTO>> getReclamacionById(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to get Reclamacion by id: {}", id);

        return reclamacionRepository
            .findById(id)
            .filter(Reclamacion::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reclamación no encontrada")))
            .flatMap(this::enrichReclamacionDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<ReclamacionDTO>> createReclamacion(Mono<ReclamacionCreateDTO> createDTO, ServerWebExchange exchange) {
        LOG.debug("REST request to create Reclamacion");

        return createDTO
            .flatMap(dto -> {
                // Validaciones básicas
                if (dto.getDescripcion() == null || dto.getDescripcion().trim().length() < 10) {
                    return Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción debe tener al menos 10 caracteres")
                    );
                }

                // Verificar que el testigo existe
                return testigoRepository
                    .findById(dto.getTestigoId())
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Testigo no encontrado")))
                    .flatMap(testigo -> {
                        // Verificar que el testigo tiene al menos una asignación activa
                        Mono<Boolean> hasAssignment = Mono.zip(
                            testigoMesaRepository.findByTestigoIdAndActivoTrue(dto.getTestigoId()).hasElements(),
                            testigoComisionRepository.findByTestigoIdAndActivoTrue(dto.getTestigoId()).hasElements()
                        ).map(tuple -> tuple.getT1() || tuple.getT2());

                        return hasAssignment.flatMap(hasAny -> {
                            if (!hasAny) {
                                return Mono.error(
                                    new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "El testigo debe tener al menos una asignación activa para presentar reclamaciones"
                                    )
                                );
                            }

                            return extractCurrentUser(exchange).flatMap(currentUser -> {
                                Reclamacion reclamacion = new Reclamacion();
                                reclamacion.setTestigoId(dto.getTestigoId());
                                reclamacion.setTipoReclamacion(dto.getTipoReclamacion().getValue());
                                reclamacion.setDescripcion(dto.getDescripcion().trim());
                                reclamacion.setEstado(ESTADO_PRESENTADA);
                                reclamacion.setMesaId(dto.getMesaId());
                                reclamacion.setComisionId(dto.getComisionId());
                                reclamacion.setFechaPresentacion(Instant.now());
                                reclamacion.setActivo(true);
                                reclamacion.setCreatedDate(Instant.now());
                                reclamacion.setLastModifiedDate(Instant.now());
                                reclamacion.setCreatedBy(currentUser);

                                return reclamacionRepository.save(reclamacion);
                            });
                        });
                    });
            })
            .flatMap(this::enrichReclamacionDTO)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<ReclamacionDTO>> resolverReclamacion(
        Long id,
        Mono<ReclamacionResolucionDTO> resolucionDTO,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to resolve Reclamacion: {}", id);

        return reclamacionRepository
            .findById(id)
            .filter(Reclamacion::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reclamación no encontrada")))
            .flatMap(reclamacion ->
                resolucionDTO.flatMap(dto -> {
                    String nuevoEstado = dto.getEstado().getValue();

                    // Validar que la reclamación no esté ya resuelta
                    if (ESTADO_ACEPTADA.equals(reclamacion.getEstado()) || ESTADO_RECHAZADA.equals(reclamacion.getEstado())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "La reclamación ya está resuelta"));
                    }

                    // Validar que el estado sea válido
                    if (!ESTADO_ACEPTADA.equals(nuevoEstado) && !ESTADO_RECHAZADA.equals(nuevoEstado)) {
                        return Mono.error(
                            new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Estado de resolución inválido. Debe ser ACEPTADA o RECHAZADA"
                            )
                        );
                    }

                    // Validar resolución
                    if (dto.getResolucion() == null || dto.getResolucion().trim().length() < 10) {
                        return Mono.error(
                            new ResponseStatusException(HttpStatus.BAD_REQUEST, "La resolución debe tener al menos 10 caracteres")
                        );
                    }

                    return extractCurrentUser(exchange).flatMap(currentUser -> {
                        reclamacion.setEstado(nuevoEstado);
                        reclamacion.setResolucion(dto.getResolucion().trim());
                        reclamacion.setFechaResolucion(Instant.now());
                        reclamacion.setResueltaPor(currentUser);
                        reclamacion.setLastModifiedDate(Instant.now());

                        return reclamacionRepository.save(reclamacion);
                    });
                })
            )
            .flatMap(this::enrichReclamacionDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteReclamacion(Long id, ServerWebExchange exchange) {
        LOG.debug("REST request to delete Reclamacion: {}", id);

        return reclamacionRepository
            .findById(id)
            .filter(Reclamacion::getActivo)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reclamación no encontrada")))
            .flatMap(reclamacion -> {
                reclamacion.setActivo(false);
                reclamacion.setLastModifiedDate(Instant.now());
                return reclamacionRepository.save(reclamacion);
            })
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<ReclamacionDTO>>> getReclamacionesByTestigo(Long testigoId, ServerWebExchange exchange) {
        LOG.debug("REST request to get Reclamaciones by testigo: {}", testigoId);

        Flux<ReclamacionDTO> reclamaciones = reclamacionRepository
            .findByTestigoIdAndActivoTrue(testigoId)
            .flatMap(this::enrichReclamacionDTO);

        return Mono.just(ResponseEntity.ok(reclamaciones));
    }

    @Override
    public Mono<ResponseEntity<Flux<ReclamacionDTO>>> getReclamacionesByMesa(Long mesaId, ServerWebExchange exchange) {
        LOG.debug("REST request to get Reclamaciones by mesa: {}", mesaId);

        Flux<ReclamacionDTO> reclamaciones = reclamacionRepository.findByMesaIdAndActivoTrue(mesaId).flatMap(this::enrichReclamacionDTO);

        return Mono.just(ResponseEntity.ok(reclamaciones));
    }

    @Override
    public Mono<ResponseEntity<Flux<ReclamacionDTO>>> getReclamacionesByComision(Long comisionId, ServerWebExchange exchange) {
        LOG.debug("REST request to get Reclamaciones by comision: {}", comisionId);

        Flux<ReclamacionDTO> reclamaciones = reclamacionRepository
            .findByComisionIdAndActivoTrue(comisionId)
            .flatMap(this::enrichReclamacionDTO);

        return Mono.just(ResponseEntity.ok(reclamaciones));
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private Mono<ReclamacionDTO> enrichReclamacionDTO(Reclamacion reclamacion) {
        Mono<TestigoElectoral> testigoMono = testigoRepository.findById(reclamacion.getTestigoId()).defaultIfEmpty(new TestigoElectoral());

        Mono<String> mesaDescMono = reclamacion.getMesaId() != null
            ? mesaVotacionRepository
                .findById(reclamacion.getMesaId())
                .map(mesa -> "Mesa " + mesa.getNumeroMesa() + " - Puesto " + mesa.getPuestoId())
                .defaultIfEmpty("")
            : Mono.just("");

        Mono<String> comisionNombreMono = reclamacion.getComisionId() != null
            ? comisionRepository.findById(reclamacion.getComisionId()).map(ComisionEscrutadora::getNombre).defaultIfEmpty("")
            : Mono.just("");

        return Mono.zip(testigoMono, mesaDescMono, comisionNombreMono).map(tuple -> {
            TestigoElectoral testigo = tuple.getT1();
            String mesaDesc = tuple.getT2();
            String comisionNombre = tuple.getT3();

            ReclamacionDTO dto = new ReclamacionDTO();
            dto.setId(reclamacion.getId());
            dto.setTestigoId(reclamacion.getTestigoId());
            dto.setTestigoNombreCompleto(testigo.getNombres() + " " + testigo.getApellidos());
            dto.setTestigoNumeroDocumento(testigo.getNumeroDocumento());
            dto.setTipoReclamacion(ReclamacionDTO.TipoReclamacionEnum.fromValue(reclamacion.getTipoReclamacion()));
            dto.setDescripcion(reclamacion.getDescripcion());
            dto.setEstado(ReclamacionDTO.EstadoEnum.fromValue(reclamacion.getEstado()));
            dto.setMesaId(reclamacion.getMesaId());
            dto.setMesaDescripcion(mesaDesc);
            dto.setComisionId(reclamacion.getComisionId());
            dto.setComisionNombre(comisionNombre);
            if (reclamacion.getFechaPresentacion() != null) {
                dto.setFechaPresentacion(OffsetDateTime.ofInstant(reclamacion.getFechaPresentacion(), ZoneOffset.UTC));
            }
            dto.setResolucion(reclamacion.getResolucion());
            if (reclamacion.getFechaResolucion() != null) {
                dto.setFechaResolucion(OffsetDateTime.ofInstant(reclamacion.getFechaResolucion(), ZoneOffset.UTC));
            }
            dto.setResueltaPor(reclamacion.getResueltaPor());
            dto.setActivo(reclamacion.getActivo());
            return dto;
        });
    }

    private Mono<String> extractCurrentUser(ServerWebExchange exchange) {
        return exchange.getPrincipal().map(principal -> principal.getName()).defaultIfEmpty("system");
    }
}

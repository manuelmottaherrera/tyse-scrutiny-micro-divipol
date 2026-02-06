package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.domain.OrganizacionPolitica;
import com.tyse.scrutiny.micro.divipol.domain.TestigoComision;
import com.tyse.scrutiny.micro.divipol.domain.TestigoElectoral;
import com.tyse.scrutiny.micro.divipol.domain.TestigoMesa;
import com.tyse.scrutiny.micro.divipol.repository.MesaVotacionRepository;
import com.tyse.scrutiny.micro.divipol.repository.OrganizacionPoliticaRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoComisionRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoElectoralRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoMesaRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoComisionAsignacionDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoComisionDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoMesaAsignacionDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoMesaDTO;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que encapsula las reglas normativas de asignación de testigos.
 *
 * Reglas implementadas:
 * - Máximo 1 testigo PRINCIPAL por organización por mesa
 * - Límite de remanentes por puesto: <10 mesas → 1 remanente, >=10 mesas → 10% del total
 * - Máximo 1 testigo por organización por comisión
 * - Verificación de período de inscripción abierto
 */
@Service
public class TestigoAsignacionService {

    private static final Logger LOG = LoggerFactory.getLogger(TestigoAsignacionService.class);

    private static final String TIPO_PRINCIPAL = "PRINCIPAL";
    private static final String TIPO_REMANENTE = "REMANENTE";

    private final TestigoMesaRepository testigoMesaRepository;
    private final TestigoComisionRepository testigoComisionRepository;
    private final TestigoElectoralRepository testigoRepository;
    private final MesaVotacionRepository mesaVotacionRepository;
    private final OrganizacionPoliticaRepository organizacionRepository;
    private final ConfiguracionElectoralService configuracionService;

    public TestigoAsignacionService(
        TestigoMesaRepository testigoMesaRepository,
        TestigoComisionRepository testigoComisionRepository,
        TestigoElectoralRepository testigoRepository,
        MesaVotacionRepository mesaVotacionRepository,
        OrganizacionPoliticaRepository organizacionRepository,
        ConfiguracionElectoralService configuracionService
    ) {
        this.testigoMesaRepository = testigoMesaRepository;
        this.testigoComisionRepository = testigoComisionRepository;
        this.testigoRepository = testigoRepository;
        this.mesaVotacionRepository = mesaVotacionRepository;
        this.organizacionRepository = organizacionRepository;
        this.configuracionService = configuracionService;
    }

    // =====================================================
    // Asignación Testigo-Mesa
    // =====================================================

    public Flux<TestigoMesaDTO> getTestigosByMesa(Long mesaId) {
        return testigoMesaRepository.findByMesaIdAndActivoTrue(mesaId).flatMap(this::enrichTestigoMesaDTO);
    }

    public Mono<TestigoMesaDTO> asignarTestigoAMesa(
        Integer puestoId,
        Long mesaId,
        TestigoMesaAsignacionDTO dto,
        ServerWebExchange exchange
    ) {
        LOG.debug("Asignando testigo {} a mesa {} del puesto {}", dto.getTestigoId(), mesaId, puestoId);

        return validateInscripcionAbierta()
            .then(
                testigoRepository
                    .findById(dto.getTestigoId())
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Testigo no encontrado")))
            )
            .flatMap(testigo -> {
                // Determinar organizacion_id (del DTO o del testigo)
                Long orgId = dto.getOrganizacionId() != null ? dto.getOrganizacionId() : testigo.getOrganizacionId();

                // Validar que no esté ya asignado
                return testigoMesaRepository
                    .existsByTestigoIdAndMesaIdAndActivoTrue(dto.getTestigoId(), mesaId)
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "El testigo ya está asignado a esta mesa"));
                        }

                        // Validar reglas normativas según tipo
                        String tipoTestigo = dto.getTipoTestigo().getValue();
                        if (TIPO_PRINCIPAL.equals(tipoTestigo)) {
                            return validatePrincipalLimit(mesaId, orgId).then(
                                createTestigoMesaAssignment(testigo, puestoId, mesaId, orgId, tipoTestigo, exchange)
                            );
                        } else {
                            return validateRemanenteLimitForPuesto(puestoId, orgId).then(
                                createTestigoMesaAssignment(testigo, puestoId, mesaId, orgId, tipoTestigo, exchange)
                            );
                        }
                    });
            });
    }

    public Mono<Void> desasignarTestigoDeMesa(Long mesaId, Long testigoId) {
        LOG.debug("Desasignando testigo {} de mesa {}", testigoId, mesaId);

        return testigoMesaRepository
            .findByTestigoIdAndMesaId(testigoId, mesaId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignación no encontrada")))
            .flatMap(tm -> testigoMesaRepository.deactivateByTestigoIdAndMesaId(testigoId, mesaId))
            .then();
    }

    private Mono<Void> validatePrincipalLimit(Long mesaId, Long orgId) {
        if (orgId == null) {
            return Mono.empty(); // Sin organización, no aplica límite
        }
        return testigoMesaRepository
            .existsByMesaIdAndOrganizacionIdAndTipoTestigoAndActivoTrue(mesaId, orgId, TIPO_PRINCIPAL)
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(
                        new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Ya existe un testigo PRINCIPAL de esta organización en la mesa"
                        )
                    );
                }
                return Mono.empty();
            });
    }

    private Mono<Void> validateRemanenteLimitForPuesto(Integer puestoId, Long orgId) {
        if (orgId == null) {
            return Mono.empty(); // Sin organización, no aplica límite
        }

        // Contar mesas del puesto y remanentes actuales de la organización
        return Mono.zip(
            mesaVotacionRepository.countByPuestoIdAndActivoTrue(puestoId),
            testigoMesaRepository.countByPuestoIdAndOrganizacionIdAndTipoTestigoAndActivoTrue(puestoId, orgId, TIPO_REMANENTE),
            configuracionService.getMaxRemanentes(0) // Placeholder, se calcula después
        ).flatMap(tuple -> {
            int numMesas = tuple.getT1().intValue();
            long currentRemanentes = tuple.getT2();

            // Calcular límite basado en número de mesas
            return configuracionService
                .getMaxRemanentes(numMesas)
                .flatMap(maxRemanentes -> {
                    if (currentRemanentes >= maxRemanentes) {
                        return Mono.error(
                            new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                String.format(
                                    "Se ha alcanzado el límite de %d remanentes para esta organización en el puesto",
                                    maxRemanentes
                                )
                            )
                        );
                    }
                    return Mono.empty();
                });
        });
    }

    private Mono<TestigoMesaDTO> createTestigoMesaAssignment(
        TestigoElectoral testigo,
        Integer puestoId,
        Long mesaId,
        Long orgId,
        String tipoTestigo,
        ServerWebExchange exchange
    ) {
        return extractCurrentUser(exchange).flatMap(currentUser -> {
            TestigoMesa tm = new TestigoMesa();
            tm.setTestigoId(testigo.getId());
            tm.setMesaId(mesaId);
            tm.setPuestoId(puestoId);
            tm.setOrganizacionId(orgId);
            tm.setTipoTestigo(tipoTestigo);
            tm.setAssignedDate(Instant.now());
            tm.setAssignedBy(currentUser);
            tm.setActivo(true);
            tm.setCreatedDate(Instant.now());
            tm.setLastModifiedDate(Instant.now());
            tm.setCreatedBy(currentUser);

            return testigoMesaRepository.save(tm).flatMap(this::enrichTestigoMesaDTO);
        });
    }

    private Mono<TestigoMesaDTO> enrichTestigoMesaDTO(TestigoMesa tm) {
        return Mono.zip(
            testigoRepository.findById(tm.getTestigoId()).defaultIfEmpty(new TestigoElectoral()),
            tm.getOrganizacionId() != null
                ? organizacionRepository.findById(tm.getOrganizacionId()).defaultIfEmpty(new OrganizacionPolitica())
                : Mono.just(new OrganizacionPolitica())
        ).map(tuple -> {
            TestigoElectoral testigo = tuple.getT1();
            OrganizacionPolitica org = tuple.getT2();

            TestigoMesaDTO dto = new TestigoMesaDTO();
            dto.setId(tm.getId());
            dto.setTestigoId(tm.getTestigoId());
            dto.setMesaId(tm.getMesaId());
            dto.setPuestoId(tm.getPuestoId());
            dto.setOrganizacionId(tm.getOrganizacionId());
            dto.setOrganizacionNombre(org.getNombre());
            dto.setTipoTestigo(TestigoMesaDTO.TipoTestigoEnum.fromValue(tm.getTipoTestigo()));
            dto.setTestigoNombreCompleto(testigo.getNombres() + " " + testigo.getApellidos());
            dto.setTestigoNumeroDocumento(testigo.getNumeroDocumento());
            if (tm.getAssignedDate() != null) {
                dto.setAssignedDate(OffsetDateTime.ofInstant(tm.getAssignedDate(), ZoneOffset.UTC));
            }
            dto.setAssignedBy(tm.getAssignedBy());
            dto.setActivo(tm.getActivo());
            return dto;
        });
    }

    // =====================================================
    // Asignación Testigo-Comisión
    // =====================================================

    public Flux<TestigoComisionDTO> getTestigosByComision(Long comisionId) {
        return testigoComisionRepository.findByComisionIdAndActivoTrue(comisionId).flatMap(this::enrichTestigoComisionDTO);
    }

    public Mono<TestigoComisionDTO> asignarTestigoAComision(Long comisionId, TestigoComisionAsignacionDTO dto, ServerWebExchange exchange) {
        LOG.debug("Asignando testigo {} a comisión {}", dto.getTestigoId(), comisionId);

        return validateInscripcionAbierta()
            .then(
                testigoRepository
                    .findById(dto.getTestigoId())
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Testigo no encontrado")))
            )
            .flatMap(testigo -> {
                Long orgId = dto.getOrganizacionId() != null ? dto.getOrganizacionId() : testigo.getOrganizacionId();

                // Validar que no esté ya asignado
                return testigoComisionRepository
                    .existsByTestigoIdAndComisionIdAndActivoTrue(dto.getTestigoId(), comisionId)
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(
                                new ResponseStatusException(HttpStatus.CONFLICT, "El testigo ya está asignado a esta comisión")
                            );
                        }

                        // Validar: máximo 1 testigo por organización por comisión
                        return validateComisionLimit(comisionId, orgId).then(
                            createTestigoComisionAssignment(testigo, comisionId, orgId, dto.getTipoTestigo().getValue(), exchange)
                        );
                    });
            });
    }

    public Mono<Void> desasignarTestigoDeComision(Long comisionId, Long testigoId) {
        LOG.debug("Desasignando testigo {} de comisión {}", testigoId, comisionId);

        return testigoComisionRepository
            .findByTestigoIdAndComisionId(testigoId, comisionId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignación no encontrada")))
            .flatMap(tc -> testigoComisionRepository.deactivateByTestigoIdAndComisionId(testigoId, comisionId))
            .then();
    }

    private Mono<Void> validateComisionLimit(Long comisionId, Long orgId) {
        if (orgId == null) {
            return Mono.empty();
        }
        return testigoComisionRepository
            .existsByComisionIdAndOrganizacionIdAndActivoTrue(comisionId, orgId)
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un testigo de esta organización en la comisión")
                    );
                }
                return Mono.empty();
            });
    }

    private Mono<TestigoComisionDTO> createTestigoComisionAssignment(
        TestigoElectoral testigo,
        Long comisionId,
        Long orgId,
        String tipoTestigo,
        ServerWebExchange exchange
    ) {
        return extractCurrentUser(exchange).flatMap(currentUser -> {
            TestigoComision tc = new TestigoComision();
            tc.setTestigoId(testigo.getId());
            tc.setComisionId(comisionId);
            tc.setOrganizacionId(orgId);
            tc.setTipoTestigo(tipoTestigo);
            tc.setAssignedDate(Instant.now());
            tc.setAssignedBy(currentUser);
            tc.setActivo(true);
            tc.setCreatedDate(Instant.now());
            tc.setLastModifiedDate(Instant.now());
            tc.setCreatedBy(currentUser);

            return testigoComisionRepository.save(tc).flatMap(this::enrichTestigoComisionDTO);
        });
    }

    private Mono<TestigoComisionDTO> enrichTestigoComisionDTO(TestigoComision tc) {
        return Mono.zip(
            testigoRepository.findById(tc.getTestigoId()).defaultIfEmpty(new TestigoElectoral()),
            tc.getOrganizacionId() != null
                ? organizacionRepository.findById(tc.getOrganizacionId()).defaultIfEmpty(new OrganizacionPolitica())
                : Mono.just(new OrganizacionPolitica())
        ).map(tuple -> {
            TestigoElectoral testigo = tuple.getT1();
            OrganizacionPolitica org = tuple.getT2();

            TestigoComisionDTO dto = new TestigoComisionDTO();
            dto.setId(tc.getId());
            dto.setTestigoId(tc.getTestigoId());
            dto.setComisionId(tc.getComisionId());
            dto.setOrganizacionId(tc.getOrganizacionId());
            dto.setOrganizacionNombre(org.getNombre());
            dto.setTipoTestigo(TestigoComisionDTO.TipoTestigoEnum.fromValue(tc.getTipoTestigo()));
            dto.setTestigoNombreCompleto(testigo.getNombres() + " " + testigo.getApellidos());
            dto.setTestigoNumeroDocumento(testigo.getNumeroDocumento());
            if (tc.getAssignedDate() != null) {
                dto.setAssignedDate(OffsetDateTime.ofInstant(tc.getAssignedDate(), ZoneOffset.UTC));
            }
            dto.setAssignedBy(tc.getAssignedBy());
            dto.setActivo(tc.getActivo());
            return dto;
        });
    }

    // =====================================================
    // Validaciones comunes
    // =====================================================

    private Mono<Void> validateInscripcionAbierta() {
        return configuracionService
            .isInscripcionAbierta()
            .flatMap(isOpen -> {
                if (!isOpen) {
                    return Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "El período de inscripción de testigos no está abierto")
                    );
                }
                return Mono.empty();
            });
    }

    private Mono<String> extractCurrentUser(ServerWebExchange exchange) {
        return exchange.getPrincipal().map(principal -> principal.getName()).defaultIfEmpty("system");
    }
}

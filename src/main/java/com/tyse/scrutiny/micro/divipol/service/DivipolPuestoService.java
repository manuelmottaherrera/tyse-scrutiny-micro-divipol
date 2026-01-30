package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.domain.Jurado;
import com.tyse.scrutiny.micro.divipol.domain.TestigoElectoral;
import com.tyse.scrutiny.micro.divipol.domain.TestigoPuesto;
import com.tyse.scrutiny.micro.divipol.repository.JuradoRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoElectoralRepository;
import com.tyse.scrutiny.micro.divipol.repository.TestigoPuestoRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.JuradoDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.PuestoDetalleDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoAsignadoDTO;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que implementa los endpoints de detalle de puesto, jurados y testigos.
 */
@Service
public class DivipolPuestoService {

    private static final Logger LOG = LoggerFactory.getLogger(DivipolPuestoService.class);

    private final DatabaseClient databaseClient;
    private final JuradoRepository juradoRepository;
    private final TestigoElectoralRepository testigoRepository;
    private final TestigoPuestoRepository testigoPuestoRepository;

    public DivipolPuestoService(
        DatabaseClient databaseClient,
        JuradoRepository juradoRepository,
        TestigoElectoralRepository testigoRepository,
        TestigoPuestoRepository testigoPuestoRepository
    ) {
        this.databaseClient = databaseClient;
        this.juradoRepository = juradoRepository;
        this.testigoRepository = testigoRepository;
        this.testigoPuestoRepository = testigoPuestoRepository;
    }

    public Mono<ResponseEntity<PuestoDetalleDTO>> getPuestoDetalle(Integer puestoId, ServerWebExchange exchange) {
        LOG.debug("REST request to get Puesto detail: {}", puestoId);

        String sql =
            """
            SELECT
                d.iddivipol,
                d.coddepto,
                d.codmipio,
                d.codzona,
                d.codpuesto,
                d.nomdepto,
                d.nommipio,
                d.nompuesto,
                d.direccion,
                ST_Y(d.cordenadas) as latitud,
                ST_X(d.cordenadas) as longitud,
                d.jal,
                d.nomjal,
                d.indicador,
                d.expandida,
                d.nummesas,
                d.potfemenino,
                d.potmasculino,
                d.pottotal
            FROM divipol d
            WHERE d.iddivipol = :puestoId AND d.clase = 'P'
            """;

        return databaseClient
            .sql(sql)
            .bind("puestoId", puestoId)
            .map((row, metadata) -> {
                PuestoDetalleDTO dto = new PuestoDetalleDTO();
                dto.setIddivipol(row.get("iddivipol", Integer.class));
                dto.setCoddepto(row.get("coddepto", Integer.class));
                dto.setCodmipio(row.get("codmipio", Integer.class));
                dto.setCodzona(row.get("codzona", Integer.class));
                dto.setCodpuesto(row.get("codpuesto", String.class));
                dto.setNomdepto(row.get("nomdepto", String.class));
                dto.setNommipio(row.get("nommipio", String.class));
                dto.setNompuesto(row.get("nompuesto", String.class));
                dto.setDireccion(row.get("direccion", String.class));
                dto.setLatitud(row.get("latitud", Double.class));
                dto.setLongitud(row.get("longitud", Double.class));
                dto.setJal(row.get("jal", Integer.class));
                dto.setNomjal(row.get("nomjal", String.class));
                dto.setIndicador(row.get("indicador", Integer.class));
                dto.setExpandida(row.get("expandida", Integer.class));
                dto.setNummesas(row.get("nummesas", Integer.class));
                dto.setPotfemenino(row.get("potfemenino", Long.class));
                dto.setPotmasculino(row.get("potmasculino", Long.class));
                dto.setPottotal(row.get("pottotal", Long.class));
                return dto;
            })
            .one()
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Puesto no encontrado")))
            .flatMap(dto ->
                // Agregar conteos de jurados y testigos
                Mono.zip(juradoRepository.countByPuestoId(puestoId), testigoPuestoRepository.countByPuestoIdAndActivoTrue(puestoId)).map(
                    tuple -> {
                        dto.setTotalJurados(tuple.getT1().intValue());
                        dto.setTotalTestigos(tuple.getT2().intValue());
                        return dto;
                    }
                )
            )
            .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<Flux<JuradoDTO>>> getJuradosByPuesto(Integer puestoId, ServerWebExchange exchange) {
        LOG.debug("REST request to get Jurados by puesto: {}", puestoId);

        Flux<JuradoDTO> jurados = juradoRepository.findByPuestoId(puestoId).map(this::toJuradoDTO);

        return Mono.just(ResponseEntity.ok(jurados));
    }

    public Mono<ResponseEntity<Flux<TestigoAsignadoDTO>>> getTestigosByPuesto(Integer puestoId, ServerWebExchange exchange) {
        LOG.debug("REST request to get Testigos by puesto: {}", puestoId);

        Flux<TestigoAsignadoDTO> testigos = testigoPuestoRepository
            .findByPuestoIdAndActivoTrue(puestoId)
            .flatMap(tp -> testigoRepository.findById(tp.getTestigoId()).map(testigo -> toTestigoAsignadoDTO(testigo, tp)));

        return Mono.just(ResponseEntity.ok(testigos));
    }

    public Mono<ResponseEntity<TestigoAsignadoDTO>> asignarTestigoAPuesto(Integer puestoId, Long testigoId, ServerWebExchange exchange) {
        LOG.debug("REST request to assign Testigo {} to Puesto {}", testigoId, puestoId);

        // Obtener usuario del JWT (si está disponible)
        String currentUser = extractCurrentUser(exchange);

        // Verificar que el testigo existe
        return testigoRepository
            .findById(testigoId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Testigo no encontrado")))
            .flatMap(testigo ->
                // Verificar que no está ya asignado
                testigoPuestoRepository
                    .existsByTestigoIdAndPuestoIdAndActivoTrue(testigoId, puestoId)
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(
                                new ResponseStatusException(HttpStatus.CONFLICT, "El testigo ya está asignado a este puesto")
                            );
                        }

                        // Crear la asignación
                        TestigoPuesto tp = new TestigoPuesto();
                        tp.setTestigoId(testigoId);
                        tp.setPuestoId(puestoId);
                        tp.setAssignedDate(Instant.now());
                        tp.setAssignedBy(currentUser);
                        tp.setActivo(true);

                        return testigoPuestoRepository.save(tp).map(saved -> toTestigoAsignadoDTO(testigo, saved));
                    })
            )
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    public Mono<ResponseEntity<Void>> desasignarTestigoDePuesto(Integer puestoId, Long testigoId, ServerWebExchange exchange) {
        LOG.debug("REST request to unassign Testigo {} from Puesto {}", testigoId, puestoId);

        return testigoPuestoRepository
            .findByTestigoIdAndPuestoId(testigoId, puestoId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignación no encontrada")))
            .flatMap(tp -> testigoPuestoRepository.deactivateByTestigoIdAndPuestoId(testigoId, puestoId))
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    // =====================================================
    // Mappers
    // =====================================================

    private JuradoDTO toJuradoDTO(Jurado jurado) {
        JuradoDTO dto = new JuradoDTO();
        dto.setId(jurado.getId());
        dto.setTipoDocumento(jurado.getTipoDocumento());
        dto.setNumeroDocumento(jurado.getNumeroDocumento());
        dto.setNombres(jurado.getNombres());
        dto.setApellidos(jurado.getApellidos());
        return dto;
    }

    private TestigoAsignadoDTO toTestigoAsignadoDTO(TestigoElectoral testigo, TestigoPuesto asignacion) {
        TestigoAsignadoDTO dto = new TestigoAsignadoDTO();
        dto.setTestigoId(testigo.getId());
        dto.setTipoDocumento(testigo.getTipoDocumento());
        dto.setNumeroDocumento(testigo.getNumeroDocumento());
        dto.setNombreCompleto(testigo.getNombres() + " " + testigo.getApellidos());
        dto.setTelefono(testigo.getTelefono());
        if (asignacion.getAssignedDate() != null) {
            dto.setAssignedDate(OffsetDateTime.ofInstant(asignacion.getAssignedDate(), ZoneOffset.UTC));
        }
        dto.setAssignedBy(asignacion.getAssignedBy());
        return dto;
    }

    private String extractCurrentUser(ServerWebExchange exchange) {
        // Intentar obtener el usuario del contexto de seguridad
        return exchange.getPrincipal().map(principal -> principal.getName()).defaultIfEmpty("system").block(); // En contexto reactivo esto debería manejarse mejor
    }
}

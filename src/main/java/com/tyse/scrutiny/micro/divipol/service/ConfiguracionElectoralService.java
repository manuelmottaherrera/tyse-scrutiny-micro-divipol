package com.tyse.scrutiny.micro.divipol.service;

import com.tyse.scrutiny.micro.divipol.domain.ConfiguracionElectoral;
import com.tyse.scrutiny.micro.divipol.repository.ConfiguracionElectoralRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ConfiguracionElectoralDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ConfiguracionElectoralUpdateDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.EstadoInscripcionDTO;
import com.tyse.scrutiny.micro.divipol.web.api.ConfiguracionApiDelegate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
 * Servicio que implementa la gestión de configuración electoral.
 * Incluye lógica para verificar período de inscripción y calcular límites de remanentes.
 */
@Service
public class ConfiguracionElectoralService implements ConfiguracionApiDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguracionElectoralService.class);

    private final ConfiguracionElectoralRepository configuracionRepository;

    public ConfiguracionElectoralService(ConfiguracionElectoralRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    public Mono<ResponseEntity<Flux<ConfiguracionElectoralDTO>>> getAllConfiguracion(ServerWebExchange exchange) {
        LOG.debug("REST request to get all Configuracion Electoral");

        Flux<ConfiguracionElectoralDTO> configs = configuracionRepository.findByActivoTrue().map(this::toDTO);
        return Mono.just(ResponseEntity.ok(configs));
    }

    @Override
    public Mono<ResponseEntity<ConfiguracionElectoralDTO>> getConfiguracionByClave(String clave, ServerWebExchange exchange) {
        LOG.debug("REST request to get Configuracion by clave: {}", clave);

        return configuracionRepository
            .findByClaveAndActivoTrue(clave)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuración no encontrada: " + clave)))
            .map(this::toDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<ConfiguracionElectoralDTO>> updateConfiguracion(
        String clave,
        Mono<ConfiguracionElectoralUpdateDTO> updateDTO,
        ServerWebExchange exchange
    ) {
        LOG.debug("REST request to update Configuracion: {}", clave);

        return configuracionRepository
            .findByClaveAndActivoTrue(clave)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuración no encontrada: " + clave)))
            .flatMap(config ->
                updateDTO.flatMap(dto -> {
                    config.setValor(dto.getValor());
                    if (dto.getDescripcion() != null) {
                        config.setDescripcion(dto.getDescripcion());
                    }
                    config.setLastModifiedDate(Instant.now());
                    return configuracionRepository.save(config);
                })
            )
            .map(this::toDTO)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<EstadoInscripcionDTO>> getEstadoInscripcion(ServerWebExchange exchange) {
        LOG.debug("REST request to get estado inscripcion");

        return Mono.zip(
            configuracionRepository
                .findByClaveAndActivoTrue("inscripcion_testigos_inicio")
                .map(ConfiguracionElectoral::getValor)
                .defaultIfEmpty(""),
            configuracionRepository
                .findByClaveAndActivoTrue("inscripcion_testigos_fin")
                .map(ConfiguracionElectoral::getValor)
                .defaultIfEmpty(""),
            configuracionRepository.findByClaveAndActivoTrue("fecha_elecciones").map(ConfiguracionElectoral::getValor).defaultIfEmpty("")
        ).map(tuple -> {
            String inicioStr = tuple.getT1();
            String finStr = tuple.getT2();
            String eleccionesStr = tuple.getT3();

            EstadoInscripcionDTO dto = new EstadoInscripcionDTO();

            if (!inicioStr.isEmpty() && !finStr.isEmpty()) {
                OffsetDateTime inicio = OffsetDateTime.parse(inicioStr);
                OffsetDateTime fin = OffsetDateTime.parse(finStr);
                OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

                dto.setFechaInicio(inicio);
                dto.setFechaFin(fin);
                dto.setAbierta(ahora.isAfter(inicio) && ahora.isBefore(fin));
            } else {
                dto.setAbierta(false);
            }

            if (!eleccionesStr.isEmpty()) {
                dto.setFechaElecciones(LocalDate.parse(eleccionesStr));
            }

            return ResponseEntity.ok(dto);
        });
    }

    // =====================================================
    // Métodos de negocio (usados por otros servicios)
    // =====================================================

    /**
     * Verifica si el período de inscripción de testigos está abierto.
     */
    public Mono<Boolean> isInscripcionAbierta() {
        return Mono.zip(
            configuracionRepository
                .findByClaveAndActivoTrue("inscripcion_testigos_inicio")
                .map(ConfiguracionElectoral::getValor)
                .defaultIfEmpty(""),
            configuracionRepository
                .findByClaveAndActivoTrue("inscripcion_testigos_fin")
                .map(ConfiguracionElectoral::getValor)
                .defaultIfEmpty("")
        ).map(tuple -> {
            String inicioStr = tuple.getT1();
            String finStr = tuple.getT2();

            if (inicioStr.isEmpty() || finStr.isEmpty()) {
                return false;
            }

            OffsetDateTime inicio = OffsetDateTime.parse(inicioStr);
            OffsetDateTime fin = OffsetDateTime.parse(finStr);
            OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

            return ahora.isAfter(inicio) && ahora.isBefore(fin);
        });
    }

    /**
     * Calcula el máximo de testigos remanentes permitidos por organización para un puesto.
     * Regla normativa: menos de 10 mesas → max_remanentes_menos_10_mesas (default 1),
     * 10+ mesas → porcentaje del total de mesas (default 10%).
     */
    public Mono<Integer> getMaxRemanentes(int numMesas) {
        if (numMesas < 10) {
            return configuracionRepository
                .findByClaveAndActivoTrue("max_remanentes_menos_10_mesas")
                .map(config -> Integer.parseInt(config.getValor()))
                .defaultIfEmpty(1);
        }

        return configuracionRepository
            .findByClaveAndActivoTrue("max_remanentes_porcentaje")
            .map(config -> {
                int porcentaje = Integer.parseInt(config.getValor());
                return (int) Math.ceil((numMesas * porcentaje) / 100.0);
            })
            .defaultIfEmpty((int) Math.ceil(numMesas * 0.1));
    }

    // =====================================================
    // Mapper
    // =====================================================

    private ConfiguracionElectoralDTO toDTO(ConfiguracionElectoral config) {
        ConfiguracionElectoralDTO dto = new ConfiguracionElectoralDTO();
        dto.setId(config.getId());
        dto.setClave(config.getClave());
        dto.setValor(config.getValor());
        dto.setDescripcion(config.getDescripcion());
        dto.setActivo(config.getActivo());
        return dto;
    }
}

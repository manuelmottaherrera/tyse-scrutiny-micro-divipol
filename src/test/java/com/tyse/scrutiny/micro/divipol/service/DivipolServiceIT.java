package com.tyse.scrutiny.micro.divipol.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.TyseScrutinyMicroDivipolApp;
import com.tyse.scrutiny.micro.divipol.config.AsyncSyncConfiguration;
import com.tyse.scrutiny.micro.divipol.config.EmbeddedSQL;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for {@link DivipolService} API endpoints.
 * Tests all divipol endpoints using real database data.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
class DivipolServiceIT {

    private static final String DIVIPOL_API = "/api/divipol";
    private static final Integer TEST_CODDEPTO = 5; // BOLIVAR
    private static final Integer TEST_CODMPIO = 1; // CARTAGENA
    private static final Integer TEST_CODZONA = 1;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build();
    }

    @Test
    void getAllDepartamentos_shouldReturnListOfDepartamentos() {
        // When: Se solicitan todos los departamentos
        List<DivipolDepartamentoDTO> departamentos = webTestClient
            .get()
            .uri(DIVIPOL_API + "/departamentos")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .returnResult(DivipolDepartamentoDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: La lista contiene departamentos
        assertThat(departamentos).isNotNull();
        assertThat(departamentos).isNotEmpty();
        assertThat(departamentos.size()).isGreaterThan(30); // Colombia tiene 32 departamentos + Bogotá D.C.

        // Then: Cada departamento tiene los campos requeridos
        DivipolDepartamentoDTO firstDepto = departamentos.get(0);
        assertThat(firstDepto.getCoddepto()).isNotNull();
        assertThat(firstDepto.getNomdepto()).isNotBlank();
        assertThat(firstDepto.getTotalPotencial()).isNotNull();
        assertThat(firstDepto.getMesas()).isNotNull();
    }

    @Test
    void getMunicipiosByDepartamento_shouldReturnMunicipiosForValidDepartment() {
        // When: Se solicitan municipios del departamento 5 (BOLIVAR)
        List<DivipolMunicipioDTO> municipios = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(DIVIPOL_API + "/municipios").queryParam("codDepto", TEST_CODDEPTO).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .returnResult(DivipolMunicipioDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: La lista contiene municipios de Bolívar
        assertThat(municipios).isNotNull();
        assertThat(municipios).isNotEmpty();
        assertThat(municipios.size()).isGreaterThan(0);

        // Then: Todos los municipios pertenecen al departamento solicitado
        assertThat(municipios).allMatch(m -> m.getCoddepto().equals(TEST_CODDEPTO));

        // Then: Los municipios tienen los campos requeridos
        DivipolMunicipioDTO firstMunicipio = municipios.get(0);
        assertThat(firstMunicipio.getCoddepto()).isEqualTo(TEST_CODDEPTO);
        assertThat(firstMunicipio.getCodmipio()).isNotNull();
        assertThat(firstMunicipio.getNomdepto()).isNotBlank();
        assertThat(firstMunicipio.getNommipio()).isNotBlank();
        assertThat(firstMunicipio.getPotencialTotal()).isNotNull();
    }

    @Test
    void getMunicipiosByDepartamento_shouldReturnEmptyForInvalidDepartment() {
        // When: Se solicitan municipios de un departamento inexistente
        List<DivipolMunicipioDTO> municipios = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(DIVIPOL_API + "/municipios").queryParam("codDepto", 999).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK (pero lista vacía)
            .expectStatus()
            .isOk()
            .returnResult(DivipolMunicipioDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: La lista está vacía
        assertThat(municipios).isEmpty();
    }

    @Test
    void getZonasByMunicipio_shouldReturnZonasForValidMunicipio() {
        // When: Se solicitan zonas del municipio 5/1 (BOLIVAR/CARTAGENA)
        List<DivipolZonaDTO> zonas = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(DIVIPOL_API + "/zonas").queryParam("codDepto", TEST_CODDEPTO).queryParam("codMpio", TEST_CODMPIO).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .returnResult(DivipolZonaDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: La lista contiene zonas
        assertThat(zonas).isNotNull();

        // Si hay zonas, verificar estructura
        if (!zonas.isEmpty()) {
            DivipolZonaDTO firstZona = zonas.get(0);
            assertThat(firstZona.getCoddepto()).isEqualTo(TEST_CODDEPTO);
            assertThat(firstZona.getCodmipio()).isEqualTo(TEST_CODMPIO);
            assertThat(firstZona.getCodzona()).isNotNull();
            assertThat(firstZona.getNomdepto()).isNotBlank();
            assertThat(firstZona.getNommipio()).isNotBlank();
        }
    }

    @Test
    void getPuestosByZona_shouldReturnPuestosForValidZona() {
        // When: Se solicitan puestos de la zona 5/1/1
        List<DivipolPuestoDTO> puestos = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(DIVIPOL_API + "/puestos")
                    .queryParam("codDepto", TEST_CODDEPTO)
                    .queryParam("codMpio", TEST_CODMPIO)
                    .queryParam("codZona", TEST_CODZONA)
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .returnResult(DivipolPuestoDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: La lista contiene puestos
        assertThat(puestos).isNotNull();

        // Si hay puestos, verificar estructura
        if (!puestos.isEmpty()) {
            DivipolPuestoDTO firstPuesto = puestos.get(0);
            assertThat(firstPuesto.getCoddepto()).isEqualTo(TEST_CODDEPTO);
            assertThat(firstPuesto.getCodmipio()).isEqualTo(TEST_CODMPIO);
            assertThat(firstPuesto.getCodzona()).isEqualTo(TEST_CODZONA);
            assertThat(firstPuesto.getCodpuesto()).isNotBlank();
            assertThat(firstPuesto.getNompuesto()).isNotBlank();
        }
    }

    @Test
    void getGeneralStats_shouldReturnGeneralStatistics() {
        // When: Se solicitan estadísticas generales
        DivipolStatsDTO stats = webTestClient
            .get()
            .uri(DIVIPOL_API + "/stats")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(DivipolStatsDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: Las estadísticas contienen datos válidos
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDepartamentos()).isGreaterThan(30L); // Colombia tiene 32 deptos + Bogotá
        assertThat(stats.getTotalMunicipios()).isGreaterThan(1000L); // Colombia tiene ~1100 municipios
        assertThat(stats.getTotalZonas()).isGreaterThan(0L);
        assertThat(stats.getTotalPuestos()).isGreaterThan(0L);
        assertThat(stats.getTotalMesas()).isGreaterThan(0L);
        assertThat(stats.getPotencialTotal()).isGreaterThan(0L);

        // Then: Validar coherencia: potencialTotal = potencialFemenino + potencialMasculino
        Long totalCalculado = stats.getPotencialFemenino() + stats.getPotencialMasculino();
        assertThat(stats.getPotencialTotal()).isEqualTo(totalCalculado);
    }

    @Test
    void getStatsByDepartamento_shouldReturnStatsForValidDepartment() {
        // When: Se solicitan estadísticas del departamento 5 (BOLIVAR)
        DivipolStatsDTO stats = webTestClient
            .get()
            .uri(DIVIPOL_API + "/stats/departamento/{codDepto}", TEST_CODDEPTO)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(DivipolStatsDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: Las estadísticas son válidas
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDepartamentos()).isEqualTo(1L);
        assertThat(stats.getTotalMunicipios()).isGreaterThan(0L);
        assertThat(stats.getPotencialTotal()).isGreaterThan(0L);

        // Then: Validar coherencia
        Long totalCalculado = stats.getPotencialFemenino() + stats.getPotencialMasculino();
        assertThat(stats.getPotencialTotal()).isEqualTo(totalCalculado);
    }

    @Test
    void getStatsByMunicipio_shouldReturnStatsForValidMunicipio() {
        // When: Se solicitan estadísticas del municipio 5/1 (BOLIVAR/CARTAGENA)
        DivipolStatsDTO stats = webTestClient
            .get()
            .uri(DIVIPOL_API + "/stats/municipio/{codDepto}/{codMpio}", TEST_CODDEPTO, TEST_CODMPIO)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(DivipolStatsDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: Las estadísticas son válidas
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDepartamentos()).isEqualTo(1L);
        assertThat(stats.getTotalMunicipios()).isEqualTo(1L);
        assertThat(stats.getTotalZonas()).isGreaterThanOrEqualTo(0L);

        // Then: Validar coherencia
        if (stats.getPotencialTotal() > 0) {
            Long totalCalculado = stats.getPotencialFemenino() + stats.getPotencialMasculino();
            assertThat(stats.getPotencialTotal()).isEqualTo(totalCalculado);
        }
    }

    @Test
    void getStatsByZona_shouldReturnStatsForValidZona() {
        // When: Se solicitan estadísticas de la zona 5/1/1
        DivipolStatsDTO stats = webTestClient
            .get()
            .uri(DIVIPOL_API + "/stats/zona/{codDepto}/{codMpio}/{codZona}", TEST_CODDEPTO, TEST_CODMPIO, TEST_CODZONA)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: Se recibe respuesta 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(DivipolStatsDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: Las estadísticas son válidas
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDepartamentos()).isEqualTo(1L);
        assertThat(stats.getTotalMunicipios()).isEqualTo(1L);
        assertThat(stats.getTotalZonas()).isEqualTo(1L);

        // Then: Validar coherencia
        if (stats.getPotencialTotal() > 0) {
            Long totalCalculado = stats.getPotencialFemenino() + stats.getPotencialMasculino();
            assertThat(stats.getPotencialTotal()).isEqualTo(totalCalculado);
        }
    }

    @Test
    void getAllDepartamentos_shouldReturnConsistentDataStructure() {
        // When: Se solicitan todos los departamentos
        Flux<DivipolDepartamentoDTO> departamentosFlux = webTestClient
            .get()
            .uri(DIVIPOL_API + "/departamentos")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(DivipolDepartamentoDTO.class)
            .getResponseBody();

        // Then: Todos los departamentos tienen estructura consistente
        departamentosFlux
            .collectList()
            .block(Duration.ofSeconds(10))
            .forEach(depto -> {
                assertThat(depto.getCoddepto()).isNotNull();
                assertThat(depto.getNomdepto()).isNotBlank();
                assertThat(depto.getMujeres()).isNotNull().isGreaterThanOrEqualTo(0L);
                assertThat(depto.getHombres()).isNotNull().isGreaterThanOrEqualTo(0L);
                assertThat(depto.getTotalPotencial()).isNotNull().isGreaterThanOrEqualTo(0L);
                assertThat(depto.getMesas()).isNotNull().isGreaterThanOrEqualTo(0L);

                // Validar coherencia: totalPotencial = mujeres + hombres
                Long totalCalculado = depto.getMujeres() + depto.getHombres();
                assertThat(depto.getTotalPotencial()).isEqualTo(totalCalculado);
            });
    }

    @Test
    void getMunicipiosByDepartamento_shouldReturnConsistentDataStructure() {
        // When: Se solicitan municipios del departamento 5 (BOLIVAR)
        Flux<DivipolMunicipioDTO> municipiosFlux = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(DIVIPOL_API + "/municipios").queryParam("codDepto", TEST_CODDEPTO).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(DivipolMunicipioDTO.class)
            .getResponseBody();

        // Then: Todos los municipios tienen estructura consistente
        municipiosFlux
            .collectList()
            .block(Duration.ofSeconds(10))
            .forEach(mpio -> {
                assertThat(mpio.getCoddepto()).isEqualTo(TEST_CODDEPTO);
                assertThat(mpio.getCodmipio()).isNotNull();
                assertThat(mpio.getNomdepto()).isNotBlank();
                assertThat(mpio.getNommipio()).isNotBlank();
                assertThat(mpio.getPotencialFemenino()).isNotNull().isGreaterThanOrEqualTo(0L);
                assertThat(mpio.getPotencialMasculino()).isNotNull().isGreaterThanOrEqualTo(0L);
                assertThat(mpio.getPotencialTotal()).isNotNull().isGreaterThanOrEqualTo(0L);
                assertThat(mpio.getMesas()).isNotNull().isGreaterThanOrEqualTo(0L);

                // Validar coherencia
                Long totalCalculado = mpio.getPotencialFemenino() + mpio.getPotencialMasculino();
                assertThat(mpio.getPotencialTotal()).isEqualTo(totalCalculado);
            });
    }
}

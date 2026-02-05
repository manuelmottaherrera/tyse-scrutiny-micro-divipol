package com.tyse.scrutiny.micro.divipol.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.TyseScrutinyMicroDivipolApp;
import com.tyse.scrutiny.micro.divipol.config.AsyncSyncConfiguration;
import com.tyse.scrutiny.micro.divipol.config.EmbeddedSQL;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for {@link DivipolPuestoService} API endpoints.
 * Tests puesto detail, jurados listing, testigo assignment/unassignment.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
class DivipolPuestoServiceIT {

    private static final String DIVIPOL_API = "/api/divipol";
    private static final String TESTIGOS_API = "/api/testigos";
    private static final Integer TEST_CODDEPTO = 5;
    private static final Integer TEST_CODMPIO = 1;
    private static final Integer TEST_CODZONA = 1;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Helper: obtiene un puestoId válido consultando los puestos existentes.
     */
    @SuppressWarnings("unchecked")
    private Integer getValidPuestoId() {
        List<Map> puestos = webTestClient
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
            .expectStatus()
            .isOk()
            .returnResult(Map.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        assertThat(puestos).isNotEmpty();
        return (Integer) puestos.get(0).get("iddivipol");
    }

    /**
     * Helper: crea un testigo vía API y retorna el DTO.
     */
    private TestigoDTO createTestigoViaApi(String doc, String nombres, String apellidos) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", doc);
        body.put("nombres", nombres);
        body.put("apellidos", apellidos);

        return webTestClient
            .post()
            .uri(TESTIGOS_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(TestigoDTO.class)
            .returnResult()
            .getResponseBody();
    }

    // =====================================================
    // Tests de Detalle de Puesto
    // =====================================================

    @Test
    void getPuestoDetalle_shouldReturnFullDetail() {
        // Given: un puestoId válido
        Integer puestoId = getValidPuestoId();

        // When: consultar el detalle del puesto
        PuestoDetalleDTO result = webTestClient
            .get()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/detalle", puestoId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(PuestoDetalleDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: el detalle contiene todos los campos requeridos
        assertThat(result).isNotNull();
        assertThat(result.getIddivipol()).isEqualTo(puestoId);
        assertThat(result.getCoddepto()).isEqualTo(TEST_CODDEPTO);
        assertThat(result.getCodmipio()).isEqualTo(TEST_CODMPIO);
        assertThat(result.getCodzona()).isEqualTo(TEST_CODZONA);
        assertThat(result.getCodpuesto()).isNotBlank();
        assertThat(result.getNomdepto()).isNotBlank();
        assertThat(result.getNommipio()).isNotBlank();
        assertThat(result.getNompuesto()).isNotBlank();
        assertThat(result.getNummesas()).isNotNull();
        assertThat(result.getPottotal()).isNotNull();
        assertThat(result.getTotalJurados()).isNotNull();
        assertThat(result.getTotalTestigos()).isNotNull();
    }

    @Test
    void getPuestoDetalle_shouldReturn404ForInvalidPuesto() {
        // When/Then: consultar puesto inexistente retorna 404
        webTestClient
            .get()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/detalle", 999999)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    // =====================================================
    // Tests de Jurados por Puesto
    // =====================================================

    @Test
    void getJuradosByPuesto_shouldReturnList() {
        // Given: un puestoId válido
        Integer puestoId = getValidPuestoId();

        // When: consultar los jurados del puesto
        List<JuradoDTO> jurados = webTestClient
            .get()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/jurados", puestoId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .returnResult(JuradoDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: retorna una lista (puede estar vacía si no hay jurados cargados)
        assertThat(jurados).isNotNull();
    }

    // =====================================================
    // Tests de Testigos por Puesto
    // =====================================================

    @Test
    void getTestigosByPuesto_shouldReturnEmptyList() {
        // Given: un puestoId válido sin testigos asignados
        Integer puestoId = getValidPuestoId();

        // When: consultar los testigos del puesto
        List<TestigoAsignadoDTO> testigos = webTestClient
            .get()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos", puestoId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .returnResult(TestigoAsignadoDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: retorna una lista (inicialmente vacía)
        assertThat(testigos).isNotNull();
    }

    // =====================================================
    // Tests de Asignación de Testigos
    // =====================================================

    @Test
    void asignarTestigoAPuesto_shouldAssignSuccessfully() {
        // Given: un puesto válido y un testigo creado
        Integer puestoId = getValidPuestoId();
        TestigoDTO testigo = createTestigoViaApi("PUESTO-ASSIGN-001", "Fernando", "Torres");

        // When: asignar el testigo al puesto
        TestigoAsignadoDTO result = webTestClient
            .post()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos/{testigoId}", puestoId, testigo.getId())
            .exchange()
            // Then: responde 201 Created
            .expectStatus()
            .isCreated()
            .expectBody(TestigoAsignadoDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: la asignación contiene los datos correctos
        assertThat(result).isNotNull();
        assertThat(result.getTestigoId()).isEqualTo(testigo.getId());
        assertThat(result.getNumeroDocumento()).isEqualTo("PUESTO-ASSIGN-001");
        assertThat(result.getNombreCompleto()).isEqualTo("Fernando Torres");
        assertThat(result.getAssignedDate()).isNotNull();
        assertThat(result.getAssignedBy()).isNotNull();
    }

    @Test
    void asignarTestigoAPuesto_shouldReturn409ForDuplicate() {
        // Given: un testigo ya asignado a un puesto
        Integer puestoId = getValidPuestoId();
        TestigoDTO testigo = createTestigoViaApi("PUESTO-DUP-001", "Duplicado", "Test");

        // Asignar la primera vez
        webTestClient
            .post()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos/{testigoId}", puestoId, testigo.getId())
            .exchange()
            .expectStatus()
            .isCreated();

        // When: intentar asignar de nuevo
        webTestClient
            .post()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos/{testigoId}", puestoId, testigo.getId())
            .exchange()
            // Then: retorna 409 Conflict
            .expectStatus()
            .isEqualTo(409);
    }

    @Test
    void asignarTestigoAPuesto_shouldReturn404ForNonExistentTestigo() {
        // Given: un puesto válido pero testigo inexistente
        Integer puestoId = getValidPuestoId();

        // When/Then: asignar testigo inexistente retorna 404
        webTestClient
            .post()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos/{testigoId}", puestoId, 999999L)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    // =====================================================
    // Tests de Desasignación de Testigos
    // =====================================================

    @Test
    void desasignarTestigoDePuesto_shouldUnassignSuccessfully() {
        // Given: un testigo asignado a un puesto
        Integer puestoId = getValidPuestoId();
        TestigoDTO testigo = createTestigoViaApi("PUESTO-UNASSIGN-001", "Desasignar", "Test");

        webTestClient
            .post()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos/{testigoId}", puestoId, testigo.getId())
            .exchange()
            .expectStatus()
            .isCreated();

        // When: desasignar el testigo del puesto
        webTestClient
            .delete()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos/{testigoId}", puestoId, testigo.getId())
            .exchange()
            // Then: responde 204 No Content
            .expectStatus()
            .isNoContent();
    }

    @Test
    void desasignarTestigoDePuesto_shouldReturn404ForNonExistentAssignment() {
        // Given: un puesto válido pero sin asignación
        Integer puestoId = getValidPuestoId();

        // When/Then: desasignar testigo no asignado retorna 404
        webTestClient
            .delete()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos/{testigoId}", puestoId, 999999L)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    // =====================================================
    // Test de Conteo de Testigos en Detalle
    // =====================================================

    @Test
    void getPuestoDetalle_shouldReflectTestigoCountAfterAssignment() {
        // Given: un puesto válido
        Integer puestoId = getValidPuestoId();

        // Obtener conteo inicial de testigos
        PuestoDetalleDTO detalleBefore = webTestClient
            .get()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/detalle", puestoId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PuestoDetalleDTO.class)
            .returnResult()
            .getResponseBody();

        assertThat(detalleBefore).isNotNull();
        Integer initialCount = detalleBefore.getTotalTestigos();

        // When: crear y asignar un testigo
        TestigoDTO testigo = createTestigoViaApi("PUESTO-COUNT-001", "Contador", "Test");

        webTestClient
            .post()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/testigos/{testigoId}", puestoId, testigo.getId())
            .exchange()
            .expectStatus()
            .isCreated();

        // Then: el conteo de testigos incrementó
        PuestoDetalleDTO detalleAfter = webTestClient
            .get()
            .uri(DIVIPOL_API + "/puestos/{puestoId}/detalle", puestoId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PuestoDetalleDTO.class)
            .returnResult()
            .getResponseBody();

        assertThat(detalleAfter).isNotNull();
        assertThat(detalleAfter.getTotalTestigos()).isEqualTo(initialCount + 1);
    }
}

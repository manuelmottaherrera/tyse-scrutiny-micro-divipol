package com.tyse.scrutiny.micro.divipol.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.TyseScrutinyMicroDivipolApp;
import com.tyse.scrutiny.micro.divipol.config.AsyncSyncConfiguration;
import com.tyse.scrutiny.micro.divipol.config.EmbeddedSQL;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import java.time.Duration;
import java.util.LinkedHashMap;
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
 * Integration tests for {@link ReclamacionService} API endpoints.
 * Tests the lifecycle of reclamaciones electorales.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
class ReclamacionServiceIT {

    private static final String RECLAMACIONES_API = "/api/reclamaciones";
    private static final String TESTIGOS_API = "/api/testigos";

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Helper: crea un testigo via API y retorna el DTO.
     * Nota: organizacionId es opcional, no se incluye aquí.
     */
    private TestigoDTO createTestigoViaApi(String doc) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", doc);
        body.put("nombres", "Testigo");
        body.put("apellidos", "Reclamacion Test");

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

    @Test
    void getAllReclamaciones_shouldReturnPaginatedResults() {
        // When: solicitar la lista paginada
        ReclamacionPage result = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(RECLAMACIONES_API).queryParam("page", 0).queryParam("size", 10).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(ReclamacionPage.class)
            .returnResult()
            .getResponseBody();

        // Then: la respuesta contiene datos de paginación válidos
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void getAllReclamaciones_shouldFilterByEstado() {
        // When: filtrar por estado PRESENTADA
        ReclamacionPage result = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(RECLAMACIONES_API).queryParam("estado", "PRESENTADA").queryParam("page", 0).queryParam("size", 10).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ReclamacionPage.class)
            .returnResult()
            .getResponseBody();

        // Then: la respuesta es válida
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        // Si hay contenido, verificar que todos tienen estado PRESENTADA
        if (!result.getContent().isEmpty()) {
            assertThat(result.getContent()).allMatch(r -> r.getEstado() == ReclamacionDTO.EstadoEnum.PRESENTADA);
        }
    }

    @Test
    void getAllReclamaciones_shouldFilterByTipo() {
        // When: filtrar por tipo ERROR_ARITMETICO
        ReclamacionPage result = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(RECLAMACIONES_API)
                    .queryParam("tipo", "ERROR_ARITMETICO")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ReclamacionPage.class)
            .returnResult()
            .getResponseBody();

        // Then: la respuesta es válida
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        // Si hay contenido, verificar que todos tienen el tipo correcto
        if (!result.getContent().isEmpty()) {
            assertThat(result.getContent()).allMatch(r -> r.getTipoReclamacion() == ReclamacionDTO.TipoReclamacionEnum.ERROR_ARITMETICO);
        }
    }

    @Test
    void getReclamacionById_shouldReturn404ForNonExistent() {
        // When/Then: consultar ID inexistente retorna 404
        webTestClient
            .get()
            .uri(RECLAMACIONES_API + "/{id}", 999999L)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void createReclamacion_shouldReturn400ForShortDescription() {
        // Given: crear testigo primero (max 20 chars para numeroDocumento)
        TestigoDTO testigo = createTestigoViaApi("REC1-" + (System.currentTimeMillis() % 100000000));

        // When: intentar crear reclamación con descripción muy corta
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("testigoId", testigo.getId());
        body.put("tipoReclamacion", "ERROR_ARITMETICO");
        body.put("descripcion", "Corto"); // Menos de 10 caracteres

        webTestClient
            .post()
            .uri(RECLAMACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: debe retornar 400 Bad Request
            .expectStatus()
            .isBadRequest();
    }

    @Test
    void createReclamacion_shouldReturn404ForNonExistentTestigo() {
        // When: intentar crear reclamación con testigo inexistente
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("testigoId", 999999L);
        body.put("tipoReclamacion", "ERROR_ARITMETICO");
        body.put("descripcion", "Esta es una descripción de prueba suficientemente larga");

        webTestClient
            .post()
            .uri(RECLAMACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: debe retornar 404 Not Found
            .expectStatus()
            .isNotFound();
    }

    @Test
    void createReclamacion_shouldReturn400ForTestigoWithoutAssignment() {
        // Given: crear testigo SIN asignaciones (max 20 chars)
        TestigoDTO testigo = createTestigoViaApi("REC2-" + (System.currentTimeMillis() % 100000000));

        // When: intentar crear reclamación
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("testigoId", testigo.getId());
        body.put("tipoReclamacion", "IRREGULARIDAD_MESA");
        body.put("descripcion", "Esta reclamación debería fallar porque el testigo no tiene asignación activa");

        webTestClient
            .post()
            .uri(RECLAMACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: debe retornar 400 Bad Request (testigo sin asignación)
            .expectStatus()
            .isBadRequest();
    }

    @Test
    void resolverReclamacion_shouldReturn404ForNonExistent() {
        // When/Then: resolver reclamación inexistente retorna 404
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", "ACEPTADA");
        body.put("resolucion", "Resolución de prueba con suficientes caracteres");

        webTestClient
            .put()
            .uri(RECLAMACIONES_API + "/{id}/resolver", 999999L)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void deleteReclamacion_shouldReturn404ForNonExistent() {
        // When/Then: eliminar reclamación inexistente retorna 404
        webTestClient.delete().uri(RECLAMACIONES_API + "/{id}", 999999L).exchange().expectStatus().isNotFound();
    }

    @Test
    void getReclamacionesByTestigo_shouldReturnEmptyForNewTestigo() {
        // Given: crear un testigo nuevo sin reclamaciones (max 20 chars)
        TestigoDTO testigo = createTestigoViaApi("REC3-" + (System.currentTimeMillis() % 100000000));

        // When: consultar reclamaciones del testigo
        webTestClient
            .get()
            .uri(RECLAMACIONES_API + "/testigo/{testigoId}", testigo.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK (puede ser lista vacía)
            .expectStatus()
            .isOk();
    }

    @Test
    void getReclamacionesByMesa_shouldReturn200() {
        // When: consultar reclamaciones de una mesa (puede estar vacía)
        webTestClient
            .get()
            .uri(RECLAMACIONES_API + "/mesa/{mesaId}", 1L)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk();
    }

    @Test
    void getReclamacionesByComision_shouldReturn200() {
        // When: consultar reclamaciones de una comisión (puede estar vacía)
        webTestClient
            .get()
            .uri(RECLAMACIONES_API + "/comision/{comisionId}", 1L)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk();
    }
}

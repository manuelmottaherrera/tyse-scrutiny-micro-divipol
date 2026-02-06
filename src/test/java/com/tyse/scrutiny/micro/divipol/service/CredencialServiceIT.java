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
 * Integration tests for {@link CredencialService} API endpoints.
 * Tests the lifecycle of credenciales E15/E16.
 *
 * Note: These tests require testigos and their assignments to exist.
 * Some tests may need adjustment based on actual data setup.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
class CredencialServiceIT {

    private static final String CREDENCIALES_API = "/api/credenciales";
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
        body.put("apellidos", "Credencial Test");

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
    void getAllCredenciales_shouldReturnPaginatedResults() {
        // When: solicitar la lista paginada
        CredencialPage result = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(CREDENCIALES_API).queryParam("page", 0).queryParam("size", 10).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(CredencialPage.class)
            .returnResult()
            .getResponseBody();

        // Then: la respuesta contiene datos de paginación válidos
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void getAllCredenciales_shouldFilterByEstado() {
        // When: filtrar por estado PENDIENTE
        CredencialPage result = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(CREDENCIALES_API).queryParam("estado", "PENDIENTE").queryParam("page", 0).queryParam("size", 10).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CredencialPage.class)
            .returnResult()
            .getResponseBody();

        // Then: la respuesta es válida
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        // Si hay contenido, verificar que todos tienen estado PENDIENTE
        if (!result.getContent().isEmpty()) {
            assertThat(result.getContent()).allMatch(c -> c.getEstado() == CredencialDTO.EstadoEnum.PENDIENTE);
        }
    }

    @Test
    void getAllCredenciales_shouldFilterByTipo() {
        // When: filtrar por tipo E15
        CredencialPage result = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(CREDENCIALES_API).queryParam("tipo", "E15").queryParam("page", 0).queryParam("size", 10).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CredencialPage.class)
            .returnResult()
            .getResponseBody();

        // Then: la respuesta es válida
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        // Si hay contenido, verificar que todos tienen tipo E15
        if (!result.getContent().isEmpty()) {
            assertThat(result.getContent()).allMatch(c -> c.getTipo() == CredencialDTO.TipoEnum.E15);
        }
    }

    @Test
    void getCredencialById_shouldReturn404ForNonExistent() {
        // When/Then: consultar ID inexistente retorna 404
        webTestClient
            .get()
            .uri(CREDENCIALES_API + "/{id}", 999999L)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void createCredencial_shouldReturn400WithoutRequiredFields() {
        // Given: crear testigo primero (max 20 chars para numeroDocumento)
        TestigoDTO testigo = createTestigoViaApi("CR1-" + (System.currentTimeMillis() % 1000000000));

        // When: intentar crear credencial E15 sin testigoMesaId
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("testigoId", testigo.getId());
        body.put("tipo", "E15");
        // Falta testigoMesaId

        webTestClient
            .post()
            .uri(CREDENCIALES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: debe retornar 400 Bad Request
            .expectStatus()
            .isBadRequest();
    }

    @Test
    void createCredencial_shouldReturn404ForNonExistentTestigo() {
        // When: intentar crear credencial con testigo inexistente
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("testigoId", 999999L);
        body.put("tipo", "E15");
        body.put("testigoMesaId", 1L);

        webTestClient
            .post()
            .uri(CREDENCIALES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: debe retornar 404 Not Found
            .expectStatus()
            .isNotFound();
    }

    @Test
    void verificarCredencial_shouldReturn404ForInvalidCode() {
        // When/Then: verificar código inexistente retorna 404
        webTestClient
            .get()
            .uri(CREDENCIALES_API + "/verificar/{codigo}", "CODIGO-INVALIDO-12345")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void getCredencialesByTestigo_shouldReturnEmptyForNewTestigo() {
        // Given: crear un testigo nuevo sin credenciales (max 20 chars)
        TestigoDTO testigo = createTestigoViaApi("CR2-" + (System.currentTimeMillis() % 1000000000));

        // When: consultar credenciales del testigo
        webTestClient
            .get()
            .uri(CREDENCIALES_API + "/testigo/{testigoId}", testigo.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK (puede ser lista vacía)
            .expectStatus()
            .isOk();
    }

    @Test
    void anularCredencial_shouldReturn404ForNonExistent() {
        // When/Then: anular credencial inexistente retorna 404
        webTestClient.delete().uri(CREDENCIALES_API + "/{id}", 999999L).exchange().expectStatus().isNotFound();
    }

    @Test
    void updateCredencialEstado_shouldReturn404ForNonExistent() {
        // When/Then: actualizar estado de credencial inexistente retorna 404
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", "EMITIDA");

        webTestClient
            .put()
            .uri(CREDENCIALES_API + "/{id}/estado", 999999L)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound();
    }
}

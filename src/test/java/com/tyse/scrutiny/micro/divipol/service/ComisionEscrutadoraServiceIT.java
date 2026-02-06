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
 * Integration tests for {@link ComisionEscrutadoraService} API endpoints.
 * Tests the CRUD operations for comisiones escrutadoras.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
class ComisionEscrutadoraServiceIT {

    private static final String COMISIONES_API = "/api/comisiones";

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Helper: crea una comisión via API y retorna el DTO de respuesta.
     */
    private ComisionEscrutadoraDTO createComisionViaApi(String nombre, String tipo, String ubicacion) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", nombre);
        body.put("tipo", tipo);
        if (ubicacion != null) {
            body.put("ubicacion", ubicacion);
        }

        return webTestClient
            .post()
            .uri(COMISIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ComisionEscrutadoraDTO.class)
            .returnResult()
            .getResponseBody();
    }

    @Test
    void createComision_shouldCreateSuccessfully() {
        // Given: datos de una nueva comisión
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Comisión Auxiliar Test " + System.currentTimeMillis());
        body.put("tipo", "AUXILIAR");
        body.put("ubicacion", "Centro de Convenciones");

        // When: se crea la comisión
        ComisionEscrutadoraDTO result = webTestClient
            .post()
            .uri(COMISIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: responde 201 Created
            .expectStatus()
            .isCreated()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(ComisionEscrutadoraDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: la comisión tiene los datos correctos
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getNombre()).contains("Comisión Auxiliar Test");
        assertThat(result.getTipo()).isEqualTo(ComisionEscrutadoraDTO.TipoEnum.AUXILIAR);
        assertThat(result.getUbicacion()).isEqualTo("Centro de Convenciones");
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    void createComision_shouldAcceptAllTipos() {
        // Test all valid tipos
        String[] tipos = { "AUXILIAR", "MUNICIPAL", "DISTRITAL", "GENERAL" };

        for (String tipo : tipos) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("nombre", "Comisión " + tipo + " " + System.currentTimeMillis());
            body.put("tipo", tipo);

            webTestClient
                .post()
                .uri(COMISIONES_API)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isCreated();
        }
    }

    @Test
    void getAllComisiones_shouldReturnPaginatedResults() {
        // Given: crear al menos dos comisiones
        String timestamp = String.valueOf(System.currentTimeMillis());
        createComisionViaApi("Comisión List 1 " + timestamp, "AUXILIAR", null);
        createComisionViaApi("Comisión List 2 " + timestamp, "MUNICIPAL", null);

        // When: solicitar la lista paginada
        ComisionEscrutadoraPage result = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(COMISIONES_API).queryParam("page", 0).queryParam("size", 10).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(ComisionEscrutadoraPage.class)
            .returnResult()
            .getResponseBody();

        // Then: la respuesta contiene datos de paginación válidos
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2L);
    }

    @Test
    void getAllComisiones_shouldFilterByTipo() {
        // Given: crear comisiones de diferentes tipos
        String timestamp = String.valueOf(System.currentTimeMillis());
        createComisionViaApi("Comisión Filter Aux " + timestamp, "AUXILIAR", null);
        createComisionViaApi("Comisión Filter Gen " + timestamp, "GENERAL", null);

        // When: filtrar por tipo GENERAL
        ComisionEscrutadoraPage result = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(COMISIONES_API).queryParam("tipo", "GENERAL").queryParam("page", 0).queryParam("size", 50).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ComisionEscrutadoraPage.class)
            .returnResult()
            .getResponseBody();

        // Then: solo retorna comisiones de tipo GENERAL
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent()).allMatch(c -> c.getTipo() == ComisionEscrutadoraDTO.TipoEnum.GENERAL);
    }

    @Test
    void getComisionById_shouldReturnExistingComision() {
        // Given: crear una comisión
        ComisionEscrutadoraDTO created = createComisionViaApi("Comisión GetById " + System.currentTimeMillis(), "MUNICIPAL", "Alcaldía");

        // When: consultar por ID
        ComisionEscrutadoraDTO result = webTestClient
            .get()
            .uri(COMISIONES_API + "/{id}", created.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectBody(ComisionEscrutadoraDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: retorna la comisión correcta
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getNombre()).isEqualTo(created.getNombre());
        assertThat(result.getUbicacion()).isEqualTo("Alcaldía");
    }

    @Test
    void getComisionById_shouldReturn404ForNonExistent() {
        // When/Then: consultar ID inexistente retorna 404
        webTestClient
            .get()
            .uri(COMISIONES_API + "/{id}", 999999L)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void updateComision_shouldUpdateFields() {
        // Given: crear una comisión
        ComisionEscrutadoraDTO created = createComisionViaApi("Comisión Update " + System.currentTimeMillis(), "AUXILIAR", null);

        // When: actualizar nombre, tipo y ubicación
        Map<String, Object> updateBody = new LinkedHashMap<>();
        updateBody.put("nombre", "Comisión Update Modificada");
        updateBody.put("tipo", "DISTRITAL");
        updateBody.put("ubicacion", "Gobernación");

        ComisionEscrutadoraDTO updated = webTestClient
            .put()
            .uri(COMISIONES_API + "/{id}", created.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBody)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectBody(ComisionEscrutadoraDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: los campos se actualizaron correctamente
        assertThat(updated).isNotNull();
        assertThat(updated.getNombre()).isEqualTo("Comisión Update Modificada");
        assertThat(updated.getTipo()).isEqualTo(ComisionEscrutadoraDTO.TipoEnum.DISTRITAL);
        assertThat(updated.getUbicacion()).isEqualTo("Gobernación");
    }

    @Test
    void updateComision_shouldReturn404ForNonExistent() {
        // When/Then: actualizar ID inexistente retorna 404
        Map<String, Object> updateBody = new LinkedHashMap<>();
        updateBody.put("nombre", "Test");
        updateBody.put("tipo", "AUXILIAR");

        webTestClient
            .put()
            .uri(COMISIONES_API + "/{id}", 999999L)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBody)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void deleteComision_shouldSoftDelete() {
        // Given: crear una comisión
        ComisionEscrutadoraDTO created = createComisionViaApi("Comisión Delete " + System.currentTimeMillis(), "AUXILIAR", null);

        // When: eliminar la comisión (soft delete)
        webTestClient.delete().uri(COMISIONES_API + "/{id}", created.getId()).exchange().expectStatus().isNoContent();

        // Then: al consultar por ID, retorna 404 (soft deleted)
        webTestClient
            .get()
            .uri(COMISIONES_API + "/{id}", created.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void deleteComision_shouldReturn404ForNonExistent() {
        // When/Then: eliminar ID inexistente retorna 404
        webTestClient.delete().uri(COMISIONES_API + "/{id}", 999999L).exchange().expectStatus().isNotFound();
    }
}

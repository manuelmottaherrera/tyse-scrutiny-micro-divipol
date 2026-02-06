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
 * Integration tests for {@link OrganizacionPoliticaService} API endpoints.
 * Tests the CRUD operations for organizaciones políticas.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
class OrganizacionPoliticaServiceIT {

    private static final String ORGANIZACIONES_API = "/api/organizaciones";

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Helper: crea una organización via API y retorna el DTO de respuesta.
     */
    private OrganizacionPoliticaDTO createOrganizacionViaApi(String nombre, String sigla, String tipo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", nombre);
        body.put("sigla", sigla);
        body.put("tipo", tipo);

        return webTestClient
            .post()
            .uri(ORGANIZACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(OrganizacionPoliticaDTO.class)
            .returnResult()
            .getResponseBody();
    }

    @Test
    void createOrganizacion_shouldCreateSuccessfully() {
        // Given: datos de una nueva organización
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Partido Test Integración");
        body.put("sigla", "PTI");
        body.put("tipo", "PARTIDO");

        // When: se crea la organización
        OrganizacionPoliticaDTO result = webTestClient
            .post()
            .uri(ORGANIZACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: responde 201 Created
            .expectStatus()
            .isCreated()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(OrganizacionPoliticaDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: la organización tiene los datos correctos
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Partido Test Integración");
        assertThat(result.getSigla()).isEqualTo("PTI");
        assertThat(result.getTipo()).isEqualTo(OrganizacionPoliticaDTO.TipoEnum.PARTIDO);
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    void createOrganizacion_shouldAcceptAllTipos() {
        // Test all valid tipos
        String[] tipos = { "PARTIDO", "MOVIMIENTO", "COALICION", "GRUPO_SIGNIFICATIVO", "COMITE_VOTO_BLANCO" };

        for (int i = 0; i < tipos.length; i++) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("nombre", "Org Tipo " + tipos[i] + " " + System.currentTimeMillis());
            body.put("tipo", tipos[i]);

            webTestClient
                .post()
                .uri(ORGANIZACIONES_API)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isCreated();
        }
    }

    @Test
    void createOrganizacion_shouldReturn409ForDuplicateName() {
        // Given: crear organización inicialmente
        createOrganizacionViaApi("Partido Duplicado Test", "PDT", "PARTIDO");

        // When: intentar crear otra con el mismo nombre
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Partido Duplicado Test");
        body.put("sigla", "PDT2");
        body.put("tipo", "MOVIMIENTO");

        webTestClient
            .post()
            .uri(ORGANIZACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: debe retornar 409 Conflict
            .expectStatus()
            .isEqualTo(409);
    }

    @Test
    void getAllOrganizaciones_shouldReturnPaginatedResults() {
        // Given: crear al menos dos organizaciones
        createOrganizacionViaApi("Org List Test 1 " + System.currentTimeMillis(), "OLT1", "PARTIDO");
        createOrganizacionViaApi("Org List Test 2 " + System.currentTimeMillis(), "OLT2", "MOVIMIENTO");

        // When: solicitar la lista paginada
        OrganizacionPoliticaPage result = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(ORGANIZACIONES_API).queryParam("page", 0).queryParam("size", 10).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(OrganizacionPoliticaPage.class)
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
    void getAllOrganizaciones_shouldFilterByTipo() {
        // Given: crear organizaciones de diferentes tipos
        String timestamp = String.valueOf(System.currentTimeMillis());
        createOrganizacionViaApi("Partido Filter " + timestamp, "PF", "PARTIDO");
        createOrganizacionViaApi("Coalicion Filter " + timestamp, "CF", "COALICION");

        // When: filtrar por tipo PARTIDO
        OrganizacionPoliticaPage result = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(ORGANIZACIONES_API).queryParam("tipo", "PARTIDO").queryParam("page", 0).queryParam("size", 50).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(OrganizacionPoliticaPage.class)
            .returnResult()
            .getResponseBody();

        // Then: solo retorna organizaciones de tipo PARTIDO
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent()).allMatch(org -> org.getTipo() == OrganizacionPoliticaDTO.TipoEnum.PARTIDO);
    }

    @Test
    void getOrganizacionById_shouldReturnExistingOrganizacion() {
        // Given: crear una organización
        OrganizacionPoliticaDTO created = createOrganizacionViaApi("Org GetById Test " + System.currentTimeMillis(), "OGT", "PARTIDO");

        // When: consultar por ID
        OrganizacionPoliticaDTO result = webTestClient
            .get()
            .uri(ORGANIZACIONES_API + "/{id}", created.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectBody(OrganizacionPoliticaDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: retorna la organización correcta
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getNombre()).isEqualTo(created.getNombre());
    }

    @Test
    void getOrganizacionById_shouldReturn404ForNonExistent() {
        // When/Then: consultar ID inexistente retorna 404
        webTestClient
            .get()
            .uri(ORGANIZACIONES_API + "/{id}", 999999L)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void updateOrganizacion_shouldUpdateFields() {
        // Given: crear una organización
        OrganizacionPoliticaDTO created = createOrganizacionViaApi("Org Update Test " + System.currentTimeMillis(), "OUT", "PARTIDO");

        // When: actualizar nombre y sigla
        Map<String, Object> updateBody = new LinkedHashMap<>();
        updateBody.put("nombre", "Org Update Test Modificada");
        updateBody.put("sigla", "OUTM");
        updateBody.put("tipo", "MOVIMIENTO");

        OrganizacionPoliticaDTO updated = webTestClient
            .put()
            .uri(ORGANIZACIONES_API + "/{id}", created.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBody)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectBody(OrganizacionPoliticaDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: los campos se actualizaron correctamente
        assertThat(updated).isNotNull();
        assertThat(updated.getNombre()).isEqualTo("Org Update Test Modificada");
        assertThat(updated.getSigla()).isEqualTo("OUTM");
        assertThat(updated.getTipo()).isEqualTo(OrganizacionPoliticaDTO.TipoEnum.MOVIMIENTO);
    }

    @Test
    void updateOrganizacion_shouldReturn404ForNonExistent() {
        // When/Then: actualizar ID inexistente retorna 404
        Map<String, Object> updateBody = new LinkedHashMap<>();
        updateBody.put("nombre", "Test");
        updateBody.put("tipo", "PARTIDO");

        webTestClient
            .put()
            .uri(ORGANIZACIONES_API + "/{id}", 999999L)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBody)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void deleteOrganizacion_shouldSoftDelete() {
        // Given: crear una organización
        OrganizacionPoliticaDTO created = createOrganizacionViaApi("Org Delete Test " + System.currentTimeMillis(), "ODT", "PARTIDO");

        // When: eliminar la organización (soft delete)
        webTestClient.delete().uri(ORGANIZACIONES_API + "/{id}", created.getId()).exchange().expectStatus().isNoContent();

        // Then: al consultar por ID, retorna 404 (soft deleted)
        webTestClient
            .get()
            .uri(ORGANIZACIONES_API + "/{id}", created.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void deleteOrganizacion_shouldReturn404ForNonExistent() {
        // When/Then: eliminar ID inexistente retorna 404
        webTestClient.delete().uri(ORGANIZACIONES_API + "/{id}", 999999L).exchange().expectStatus().isNotFound();
    }
}

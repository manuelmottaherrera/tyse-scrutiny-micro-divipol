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
 * Integration tests for {@link TestigoService} API endpoints.
 * Tests the CRUD operations for testigos electorales.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
class TestigoServiceIT {

    private static final String TESTIGOS_API = "/api/testigos";
    private static final String ORGANIZACIONES_API = "/api/organizaciones";

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Helper: crea una organización via API y retorna el DTO.
     */
    private OrganizacionPoliticaDTO createOrganizacionViaApi(String nombre) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", nombre);
        body.put("sigla", "TEST");
        body.put("tipo", "PARTIDO");

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

    /**
     * Helper: crea un testigo via API y retorna el DTO de respuesta.
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

    @Test
    void createTestigo_shouldCreateSuccessfully() {
        // Given: datos de un nuevo testigo
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", "TEST-CREATE-001");
        body.put("nombres", "Juan");
        body.put("apellidos", "Pérez");
        body.put("telefono", "3001234567");
        body.put("email", "juan@test.com");

        // When: se crea el testigo
        TestigoDTO result = webTestClient
            .post()
            .uri(TESTIGOS_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: responde 201 Created
            .expectStatus()
            .isCreated()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(TestigoDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: el testigo tiene los datos correctos
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTipoDocumento()).isEqualTo("CC");
        assertThat(result.getNumeroDocumento()).isEqualTo("TEST-CREATE-001");
        assertThat(result.getNombres()).isEqualTo("Juan");
        assertThat(result.getApellidos()).isEqualTo("Pérez");
        assertThat(result.getTelefono()).isEqualTo("3001234567");
        assertThat(result.getEmail()).isEqualTo("juan@test.com");
        assertThat(result.getActivo()).isTrue();
        assertThat(result.getPuestosAsignados()).isEqualTo(0);
    }

    @Test
    void createTestigo_shouldReturn409ForDuplicateDocument() {
        // Given: crear testigo inicialmente
        createTestigoViaApi("TEST-DUP-001", "Carlos", "García");

        // When: intentar crear otro con el mismo documento
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", "TEST-DUP-001");
        body.put("nombres", "Otro");
        body.put("apellidos", "Nombre");

        webTestClient
            .post()
            .uri(TESTIGOS_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: debe retornar 409 Conflict
            .expectStatus()
            .isEqualTo(409);
    }

    @Test
    void createTestigo_shouldReactivateInactiveTestigo() {
        // Given: crear y eliminar un testigo (soft delete)
        TestigoDTO created = createTestigoViaApi("TEST-REACT-001", "Ana", "Martínez");

        webTestClient.delete().uri(TESTIGOS_API + "/{id}", created.getId()).exchange().expectStatus().isNoContent();

        // When: crear de nuevo con el mismo documento
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", "TEST-REACT-001");
        body.put("nombres", "Ana María");
        body.put("apellidos", "Martínez López");

        TestigoDTO reactivated = webTestClient
            .post()
            .uri(TESTIGOS_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            // Then: debe crear (reactivar) exitosamente
            .expectStatus()
            .isCreated()
            .expectBody(TestigoDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: el testigo reactivado tiene los nuevos datos
        assertThat(reactivated).isNotNull();
        assertThat(reactivated.getNombres()).isEqualTo("Ana María");
        assertThat(reactivated.getApellidos()).isEqualTo("Martínez López");
        assertThat(reactivated.getActivo()).isTrue();
    }

    @Test
    void getAllTestigos_shouldReturnPaginatedResults() {
        // Given: crear al menos dos testigos
        createTestigoViaApi("TEST-LIST-001", "Pedro", "Sánchez");
        createTestigoViaApi("TEST-LIST-002", "María", "López");

        // When: solicitar la lista paginada
        TestigoPage result = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(TESTIGOS_API).queryParam("page", 0).queryParam("size", 10).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(TestigoPage.class)
            .returnResult()
            .getResponseBody();

        // Then: la respuesta contiene datos de paginación válidos
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2L);
        assertThat(result.getTotalPages()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getTestigoById_shouldReturnExistingTestigo() {
        // Given: crear un testigo
        TestigoDTO created = createTestigoViaApi("TEST-GETID-001", "Luis", "Rodríguez");

        // When: consultar por ID
        TestigoDTO result = webTestClient
            .get()
            .uri(TESTIGOS_API + "/{id}", created.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(TestigoDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: retorna el testigo correcto
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getNumeroDocumento()).isEqualTo("TEST-GETID-001");
        assertThat(result.getNombres()).isEqualTo("Luis");
        assertThat(result.getApellidos()).isEqualTo("Rodríguez");
    }

    @Test
    void getTestigoById_shouldReturn404ForNonExistent() {
        // When/Then: consultar ID inexistente retorna 404
        webTestClient.get().uri(TESTIGOS_API + "/{id}", 999999L).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound();
    }

    @Test
    void updateTestigo_shouldUpdatePartialFields() {
        // Given: crear un testigo
        TestigoDTO created = createTestigoViaApi("TEST-UPDATE-001", "Roberto", "Gómez");

        // When: actualizar nombres y agregar contacto
        Map<String, Object> updateBody = new LinkedHashMap<>();
        updateBody.put("nombres", "Roberto Carlos");
        updateBody.put("apellidos", "Gómez Bolaños");
        updateBody.put("telefono", "3009876543");
        updateBody.put("email", "roberto@test.com");

        TestigoDTO updated = webTestClient
            .put()
            .uri(TESTIGOS_API + "/{id}", created.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBody)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectBody(TestigoDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: los campos se actualizaron correctamente
        assertThat(updated).isNotNull();
        assertThat(updated.getNombres()).isEqualTo("Roberto Carlos");
        assertThat(updated.getApellidos()).isEqualTo("Gómez Bolaños");
        assertThat(updated.getTelefono()).isEqualTo("3009876543");
        assertThat(updated.getEmail()).isEqualTo("roberto@test.com");
        // El documento no cambia
        assertThat(updated.getNumeroDocumento()).isEqualTo("TEST-UPDATE-001");
    }

    @Test
    void updateTestigo_shouldReturn404ForNonExistent() {
        // When/Then: actualizar ID inexistente retorna 404
        Map<String, Object> updateBody = new LinkedHashMap<>();
        updateBody.put("nombres", "Test");

        webTestClient
            .put()
            .uri(TESTIGOS_API + "/{id}", 999999L)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBody)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void deleteTestigo_shouldSoftDelete() {
        // Given: crear un testigo
        TestigoDTO created = createTestigoViaApi("TEST-DELETE-001", "Sandra", "Díaz");

        // When: eliminar el testigo (soft delete)
        webTestClient.delete().uri(TESTIGOS_API + "/{id}", created.getId()).exchange().expectStatus().isNoContent();

        // Then: al consultar por ID, retorna 404 (soft deleted)
        webTestClient
            .get()
            .uri(TESTIGOS_API + "/{id}", created.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void deleteTestigo_shouldReturn404ForNonExistent() {
        // When/Then: eliminar ID inexistente retorna 404
        webTestClient.delete().uri(TESTIGOS_API + "/{id}", 999999L).exchange().expectStatus().isNotFound();
    }

    @Test
    void searchTestigos_shouldFindByNameOrDocument() {
        // Given: crear un testigo con nombre distintivo
        createTestigoViaApi("TEST-SEARCH-001", "Zacarías", "Zambrano");

        // When: buscar por nombre
        TestigoPage result = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(TESTIGOS_API + "/search").queryParam("q", "Zacarías").queryParam("page", 0).queryParam("size", 10).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // Then: responde 200 OK
            .expectStatus()
            .isOk()
            .expectBody(TestigoPage.class)
            .returnResult()
            .getResponseBody();

        // Then: debe encontrar el testigo
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getNombres()).isEqualTo("Zacarías");
    }

    @Test
    void searchTestigos_shouldReturn400ForShortQuery() {
        // When/Then: buscar con query menor a 2 caracteres retorna 400
        webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(TESTIGOS_API + "/search").queryParam("q", "a").queryParam("page", 0).queryParam("size", 10).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    // =====================================================
    // Tests para organizacionId
    // =====================================================

    @Test
    void createTestigo_shouldSaveOrganizacionId() {
        // Given: crear una organización primero
        OrganizacionPoliticaDTO org = createOrganizacionViaApi("Partido Test OrgId " + System.currentTimeMillis());

        // When: crear testigo con organizacionId
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", "ORG-" + (System.currentTimeMillis() % 100000000));
        body.put("nombres", "Testigo");
        body.put("apellidos", "Con Organización");
        body.put("organizacionId", org.getId());

        TestigoDTO result = webTestClient
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

        // Then: organizacionId se guarda y retorna correctamente
        assertThat(result).isNotNull();
        assertThat(result.getOrganizacionId()).isEqualTo(org.getId());
    }

    @Test
    void createTestigo_shouldAllowNullOrganizacionId() {
        // When: crear testigo sin organizacionId
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", "NOORG-" + (System.currentTimeMillis() % 100000000));
        body.put("nombres", "Testigo");
        body.put("apellidos", "Sin Organización");

        TestigoDTO result = webTestClient
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

        // Then: testigo se crea con organizacionId null
        assertThat(result).isNotNull();
        assertThat(result.getOrganizacionId()).isNull();
    }

    @Test
    void updateTestigo_shouldUpdateOrganizacionId() {
        // Given: crear organización y testigo sin organización
        OrganizacionPoliticaDTO org = createOrganizacionViaApi("Partido Update " + System.currentTimeMillis());
        TestigoDTO created = createTestigoViaApi("UPDORG-" + (System.currentTimeMillis() % 100000000), "Testigo", "Actualizar Org");

        // When: actualizar testigo con organizacionId
        Map<String, Object> updateBody = new LinkedHashMap<>();
        updateBody.put("organizacionId", org.getId());

        TestigoDTO updated = webTestClient
            .put()
            .uri(TESTIGOS_API + "/{id}", created.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBody)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TestigoDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: organizacionId se actualiza
        assertThat(updated).isNotNull();
        assertThat(updated.getOrganizacionId()).isEqualTo(org.getId());
    }

    @Test
    void reactivateTestigo_shouldUpdateOrganizacionId() {
        // Given: crear organización y testigo, luego eliminar testigo
        OrganizacionPoliticaDTO org = createOrganizacionViaApi("Partido Reactivar " + System.currentTimeMillis());
        String doc = "REACT-" + (System.currentTimeMillis() % 100000000);
        TestigoDTO created = createTestigoViaApi(doc, "Testigo", "Reactivar");
        webTestClient.delete().uri(TESTIGOS_API + "/{id}", created.getId()).exchange().expectStatus().isNoContent();

        // When: reactivar testigo con organizacionId
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", doc);
        body.put("nombres", "Testigo Reactivado");
        body.put("apellidos", "Con Nueva Org");
        body.put("organizacionId", org.getId());

        TestigoDTO reactivated = webTestClient
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

        // Then: organizacionId se guarda en testigo reactivado
        assertThat(reactivated).isNotNull();
        assertThat(reactivated.getOrganizacionId()).isEqualTo(org.getId());
    }

    @Test
    void getTestigoById_shouldReturnOrganizacionId() {
        // Given: crear organización y testigo con organizacionId
        OrganizacionPoliticaDTO org = createOrganizacionViaApi("Partido GetById " + System.currentTimeMillis());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", "GETORG-" + (System.currentTimeMillis() % 100000000));
        body.put("nombres", "Testigo");
        body.put("apellidos", "Consultar Org");
        body.put("organizacionId", org.getId());

        TestigoDTO created = webTestClient
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

        // When: consultar testigo por ID
        TestigoDTO result = webTestClient
            .get()
            .uri(TESTIGOS_API + "/{id}", created.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TestigoDTO.class)
            .returnResult()
            .getResponseBody();

        // Then: organizacionId se retorna
        assertThat(result).isNotNull();
        assertThat(result.getOrganizacionId()).isEqualTo(org.getId());
    }
}

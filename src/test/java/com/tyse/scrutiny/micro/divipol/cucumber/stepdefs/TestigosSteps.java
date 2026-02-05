package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.security.jwt.JwtAuthenticationTestUtils;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoPage;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Y;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Step definitions para escenarios de testigos electorales.
 *
 * Esta clase define los steps específicos para CRUD de testigos.
 * Los steps comunes (autenticación, GET, validación de status code)
 * están en DivipolCommonSteps y NO se deben duplicar aquí.
 */
public class TestigosSteps extends StepDefs {

    private static final String TESTIGOS_API = "/api/testigos";

    @Autowired
    protected WebTestClient webTestClient;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    protected String jwtKey;

    // Estado compartido dentro de un escenario
    private TestigoDTO lastCreatedTestigo;
    private TestigoPage lastTestigoPage;

    // =====================================================
    // Helper: crear testigo via API
    // =====================================================

    private TestigoDTO createTestigoHelper(String doc, String nombres, String apellidos) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", doc);
        body.put("nombres", nombres);
        body.put("apellidos", apellidos);

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        return webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .post()
            .uri(TESTIGOS_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(TestigoDTO.class)
            .returnResult()
            .getResponseBody();
    }

    // =====================================================
    // Steps: Precondiciones (Dado)
    // =====================================================

    @Dado("que existe un testigo con documento {string}")
    public void queExisteUnTestigoConDocumento(String documento) {
        lastCreatedTestigo = createTestigoHelper(documento, "TestNombre", "TestApellido");
        assertThat(lastCreatedTestigo).isNotNull();
        assertThat(lastCreatedTestigo.getId()).isNotNull();
    }

    @Dado("que existe un testigo con documento {string} y nombre {string}")
    public void queExisteUnTestigoConDocumentoYNombre(String documento, String nombre) {
        lastCreatedTestigo = createTestigoHelper(documento, nombre, "TestApellido");
        assertThat(lastCreatedTestigo).isNotNull();
    }

    // =====================================================
    // Steps: Acciones (Cuando)
    // =====================================================

    @Cuando("creo un testigo con los datos:")
    public void creoUnTestigoConLosDatos(io.cucumber.datatable.DataTable dataTable) {
        lastCreatedTestigo = null;

        Map<String, String> datos = dataTable.asMap(String.class, String.class);
        Map<String, Object> body = new LinkedHashMap<>();
        datos.forEach(body::put);

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .post()
            .uri(TESTIGOS_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();

        // Intentar capturar el testigo creado (puede fallar si la respuesta no es 2xx)
        try {
            lastCreatedTestigo = actions.expectBody(TestigoDTO.class).returnResult().getResponseBody();
        } catch (AssertionError e) {
            lastCreatedTestigo = null;
        }
    }

    @Cuando("consulto el testigo por su ID")
    public void consultoElTestigoPorSuID() {
        assertThat(lastCreatedTestigo).as("Debe existir un testigo previamente creado").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(TESTIGOS_API + "/{id}", lastCreatedTestigo.getId())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    @Cuando("consulto el testigo con ID {long}")
    public void consultoElTestigoConID(long id) {
        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(TESTIGOS_API + "/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    @Cuando("actualizo el testigo con los datos:")
    public void actualizoElTestigoConLosDatos(io.cucumber.datatable.DataTable dataTable) {
        assertThat(lastCreatedTestigo).as("Debe existir un testigo previamente creado").isNotNull();

        Map<String, String> datos = dataTable.asMap(String.class, String.class);
        Map<String, Object> body = new LinkedHashMap<>();
        datos.forEach(body::put);

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .put()
            .uri(TESTIGOS_API + "/{id}", lastCreatedTestigo.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();

        // Intentar capturar el testigo actualizado
        try {
            lastCreatedTestigo = actions.expectBody(TestigoDTO.class).returnResult().getResponseBody();
        } catch (AssertionError e) {
            // Puede fallar si el update no es exitoso
        }
    }

    @Cuando("elimino el testigo")
    public void eliminoElTestigo() {
        assertThat(lastCreatedTestigo).as("Debe existir un testigo previamente creado").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .delete()
            .uri(TESTIGOS_API + "/{id}", lastCreatedTestigo.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    @Cuando("busco testigos con el término {string}")
    public void buscoTestigosConElTermino(String termino) {
        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(TESTIGOS_API + "/search").queryParam("q", termino).queryParam("page", 0).queryParam("size", 10).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    // =====================================================
    // Steps: Validaciones (Y/Entonces)
    // =====================================================

    @Y("el testigo creado tiene nombre {string}")
    public void elTestigoCreadoTieneNombre(String nombre) {
        if (lastCreatedTestigo == null) {
            lastCreatedTestigo = actions.expectBody(TestigoDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedTestigo).isNotNull();
        assertThat(lastCreatedTestigo.getNombres()).isEqualTo(nombre);
    }

    @Y("el testigo creado tiene apellido {string}")
    public void elTestigoCreadoTieneApellido(String apellido) {
        if (lastCreatedTestigo == null) {
            lastCreatedTestigo = actions.expectBody(TestigoDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedTestigo).isNotNull();
        assertThat(lastCreatedTestigo.getApellidos()).isEqualTo(apellido);
    }

    @Y("el testigo creado está activo")
    public void elTestigoCreadoEstaActivo() {
        if (lastCreatedTestigo == null) {
            lastCreatedTestigo = actions.expectBody(TestigoDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedTestigo).isNotNull();
        assertThat(lastCreatedTestigo.getActivo()).isTrue();
    }

    @Y("el testigo tiene documento {string}")
    public void elTestigoTieneDocumento(String documento) {
        TestigoDTO testigo = actions.expectBody(TestigoDTO.class).returnResult().getResponseBody();

        assertThat(testigo).isNotNull();
        assertThat(testigo.getNumeroDocumento()).isEqualTo(documento);
    }

    @Y("la respuesta contiene una lista paginada de testigos")
    public void laRespuestaContieneUnaListaPaginadaDeTestigos() {
        lastTestigoPage = actions.expectBody(TestigoPage.class).returnResult().getResponseBody();

        assertThat(lastTestigoPage).isNotNull();
        assertThat(lastTestigoPage.getContent()).isNotNull();
        assertThat(lastTestigoPage.getTotalElements()).isGreaterThanOrEqualTo(1L);
    }

    @Y("al consultar el testigo eliminado recibo {int}")
    public void alConsultarElTestigoEliminadoRecibo(int statusCode) {
        assertThat(lastCreatedTestigo).as("Debe existir un testigo previamente creado").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(TESTIGOS_API + "/{id}", lastCreatedTestigo.getId())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus()
            .isEqualTo(statusCode);
    }

    @Y("la búsqueda contiene resultados")
    public void laBusquedaContieneResultados() {
        lastTestigoPage = actions.expectBody(TestigoPage.class).returnResult().getResponseBody();

        assertThat(lastTestigoPage).isNotNull();
        assertThat(lastTestigoPage.getContent()).isNotNull();
        assertThat(lastTestigoPage.getContent()).isNotEmpty();
    }
}

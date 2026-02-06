package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.security.jwt.JwtAuthenticationTestUtils;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ReclamacionDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ReclamacionPage;
import com.tyse.scrutiny.micro.divipol.service.api.dto.TestigoDTO;
import io.cucumber.java.es.Cuando;
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
 * Step definitions para escenarios de reclamaciones electorales.
 */
public class ReclamacionesSteps extends StepDefs {

    private static final String RECLAMACIONES_API = "/api/reclamaciones";
    private static final String TESTIGOS_API = "/api/testigos";

    @Autowired
    protected WebTestClient webTestClient;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    protected String jwtKey;

    // Estado compartido dentro de un escenario
    private ReclamacionDTO lastCreatedReclamacion;
    private ReclamacionPage lastReclamacionPage;
    private TestigoDTO lastCreatedTestigo;

    // =====================================================
    // Helper: crear testigo via API
    // =====================================================

    private TestigoDTO createTestigoHelper(String doc) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipoDocumento", "CC");
        body.put("numeroDocumento", doc);
        body.put("nombres", "Testigo");
        body.put("apellidos", "Reclamacion");

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
    // Steps: Acciones (Cuando)
    // =====================================================

    @Cuando("consulto la reclamación con ID {long}")
    public void consultoLaReclamacionConID(long id) {
        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(RECLAMACIONES_API + "/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    @Cuando("creo una reclamación con testigo inexistente")
    public void creoUnaReclamacionConTestigoInexistente() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("testigoId", 999999L);
        body.put("tipoReclamacion", "ERROR_ARITMETICO");
        body.put("descripcion", "Esta es una descripción de prueba suficientemente larga para pasar la validación");

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .post()
            .uri(RECLAMACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();
    }

    @Cuando("creo una reclamación con descripción corta")
    public void creoUnaReclamacionConDescripcionCorta() {
        assertThat(sharedTestigo).as("Debe existir un testigo previamente creado").isNotNull();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("testigoId", sharedTestigo.getId());
        body.put("tipoReclamacion", "ERROR_ARITMETICO");
        body.put("descripcion", "Corto"); // Menos de 10 caracteres

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .post()
            .uri(RECLAMACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();
    }

    @Cuando("resuelvo la reclamación con ID {long}")
    public void resuelvoLaReclamacionConID(long id) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", "ACEPTADA");
        body.put("resolucion", "Resolución de prueba con suficientes caracteres para la validación");

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .put()
            .uri(RECLAMACIONES_API + "/{id}/resolver", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();
    }

    @Cuando("consulto reclamaciones del testigo")
    public void consultoReclamacionesDelTestigo() {
        assertThat(sharedTestigo).as("Debe existir un testigo previamente creado").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(RECLAMACIONES_API + "/testigo/{testigoId}", sharedTestigo.getId())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    // =====================================================
    // Steps: Validaciones (Y/Entonces)
    // =====================================================

    @Y("la respuesta contiene una lista paginada de reclamaciones")
    public void laRespuestaContieneUnaListaPaginadaDeReclamaciones() {
        lastReclamacionPage = actions.expectBody(ReclamacionPage.class).returnResult().getResponseBody();

        assertThat(lastReclamacionPage).isNotNull();
        assertThat(lastReclamacionPage.getContent()).isNotNull();
    }
}

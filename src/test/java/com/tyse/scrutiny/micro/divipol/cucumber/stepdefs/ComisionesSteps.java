package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.security.jwt.JwtAuthenticationTestUtils;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ComisionEscrutadoraDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.ComisionEscrutadoraPage;
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
 * Step definitions para escenarios de comisiones escrutadoras.
 */
public class ComisionesSteps extends StepDefs {

    private static final String COMISIONES_API = "/api/comisiones";

    @Autowired
    protected WebTestClient webTestClient;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    protected String jwtKey;

    // Estado compartido dentro de un escenario
    private ComisionEscrutadoraDTO lastCreatedComision;
    private ComisionEscrutadoraPage lastComisionPage;

    // =====================================================
    // Helper: crear comisión via API
    // =====================================================

    private ComisionEscrutadoraDTO createComisionHelper(String nombre, String tipo, String ubicacion) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", nombre);
        body.put("tipo", tipo != null ? tipo : "AUXILIAR");
        body.put("ubicacion", ubicacion != null ? ubicacion : "Ubicación BDD");

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        return webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .post()
            .uri(COMISIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ComisionEscrutadoraDTO.class)
            .returnResult()
            .getResponseBody();
    }

    // =====================================================
    // Steps: Precondiciones (Dado)
    // =====================================================

    @Dado("que existe una comisión escrutadora {string}")
    public void queExisteUnaComisionEscrutadora(String nombre) {
        lastCreatedComision = createComisionHelper(nombre, "AUXILIAR", "Ubicación BDD");
        assertThat(lastCreatedComision).isNotNull();
        assertThat(lastCreatedComision.getId()).isNotNull();
    }

    // =====================================================
    // Steps: Acciones (Cuando)
    // =====================================================

    @Cuando("creo una comisión escrutadora con los datos:")
    public void creoUnaComisionEscrutadoraConLosDatos(io.cucumber.datatable.DataTable dataTable) {
        lastCreatedComision = null;

        Map<String, String> datos = dataTable.asMap(String.class, String.class);
        Map<String, Object> body = new LinkedHashMap<>();
        datos.forEach(body::put);

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .post()
            .uri(COMISIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();

        // Intentar capturar la comisión creada
        try {
            lastCreatedComision = actions.expectBody(ComisionEscrutadoraDTO.class).returnResult().getResponseBody();
        } catch (AssertionError e) {
            lastCreatedComision = null;
        }
    }

    @Cuando("consulto la comisión por su ID")
    public void consultoLaComisionPorSuID() {
        assertThat(lastCreatedComision).as("Debe existir una comisión previamente creada").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(COMISIONES_API + "/{id}", lastCreatedComision.getId())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    @Cuando("consulto la comisión con ID {long}")
    public void consultoLaComisionConID(long id) {
        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(COMISIONES_API + "/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    @Cuando("actualizo la comisión con los datos:")
    public void actualizoLaComisionConLosDatos(io.cucumber.datatable.DataTable dataTable) {
        assertThat(lastCreatedComision).as("Debe existir una comisión previamente creada").isNotNull();

        Map<String, String> datos = dataTable.asMap(String.class, String.class);
        Map<String, Object> body = new LinkedHashMap<>();
        datos.forEach(body::put);

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .put()
            .uri(COMISIONES_API + "/{id}", lastCreatedComision.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();

        // Intentar capturar la comisión actualizada
        try {
            lastCreatedComision = actions.expectBody(ComisionEscrutadoraDTO.class).returnResult().getResponseBody();
        } catch (AssertionError e) {
            // Puede fallar si el update no es exitoso
        }
    }

    @Cuando("elimino la comisión")
    public void eliminoLaComision() {
        assertThat(lastCreatedComision).as("Debe existir una comisión previamente creada").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .delete()
            .uri(COMISIONES_API + "/{id}", lastCreatedComision.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    // =====================================================
    // Steps: Validaciones (Y/Entonces)
    // =====================================================

    @Y("la comisión creada tiene nombre {string}")
    public void laComisionCreadaTieneNombre(String nombre) {
        if (lastCreatedComision == null) {
            lastCreatedComision = actions.expectBody(ComisionEscrutadoraDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedComision).isNotNull();
        assertThat(lastCreatedComision.getNombre()).isEqualTo(nombre);
    }

    @Y("la comisión creada tiene tipo {string}")
    public void laComisionCreadaTieneTipo(String tipo) {
        if (lastCreatedComision == null) {
            lastCreatedComision = actions.expectBody(ComisionEscrutadoraDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedComision).isNotNull();
        assertThat(lastCreatedComision.getTipo().toString()).isEqualTo(tipo);
    }

    @Y("la comisión creada está activa")
    public void laComisionCreadaEstaActiva() {
        if (lastCreatedComision == null) {
            lastCreatedComision = actions.expectBody(ComisionEscrutadoraDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedComision).isNotNull();
        assertThat(lastCreatedComision.getActivo()).isTrue();
    }

    @Y("la comisión tiene nombre {string}")
    public void laComisionTieneNombre(String nombre) {
        ComisionEscrutadoraDTO comision = actions.expectBody(ComisionEscrutadoraDTO.class).returnResult().getResponseBody();

        assertThat(comision).isNotNull();
        assertThat(comision.getNombre()).isEqualTo(nombre);
    }

    @Y("la respuesta contiene una lista paginada de comisiones")
    public void laRespuestaContieneUnaListaPaginadaDeComisiones() {
        lastComisionPage = actions.expectBody(ComisionEscrutadoraPage.class).returnResult().getResponseBody();

        assertThat(lastComisionPage).isNotNull();
        assertThat(lastComisionPage.getContent()).isNotNull();
        assertThat(lastComisionPage.getTotalElements()).isGreaterThanOrEqualTo(1L);
    }

    @Y("al consultar la comisión eliminada recibo {int}")
    public void alConsultarLaComisionEliminadaRecibo(int statusCode) {
        assertThat(lastCreatedComision).as("Debe existir una comisión previamente creada").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(COMISIONES_API + "/{id}", lastCreatedComision.getId())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus()
            .isEqualTo(statusCode);
    }
}

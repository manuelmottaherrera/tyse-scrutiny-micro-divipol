package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.security.jwt.JwtAuthenticationTestUtils;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.spring.CucumberContextConfiguration;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Step definitions comunes compartidos por todos los escenarios de Cucumber.
 *
 * Esta clase define los steps que son comunes a múltiples features
 * (autenticación, peticiones HTTP, validación de respuestas).
 */
public class DivipolCommonSteps extends StepDefs {

    @Autowired
    protected WebTestClient webTestClient;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    protected String jwtKey;

    protected boolean authenticated = true;

    // ===== STEPS COMUNES =====

    @Dado("que la base de datos tiene cargados los datos de DIVIPOL")
    public void queBaseDeDatosTieneDatosDeDivipol() {
        assertThat(webTestClient).isNotNull();
    }

    @Dado("que soy un usuario autenticado")
    public void queSoyUnUsuarioAutenticado() {
        authenticated = true;
    }

    @Dado("que NO estoy autenticado")
    public void queNoEstoyAutenticado() {
        authenticated = false;
    }

    @Cuando("consulto el endpoint GET {string}")
    public void consultoElEndpointGET(String endpoint) {
        WebTestClient.RequestHeadersSpec<?> request = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(endpoint)
            .accept(MediaType.APPLICATION_JSON);

        if (authenticated) {
            String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);
            actions = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange();
        } else {
            actions = request.exchange();
        }
    }

    @Entonces("recibo un código de respuesta {int}")
    public void reciboUnCodigoDeRespuesta(int statusCode) {
        actions.expectStatus().isEqualTo(statusCode);
    }
}

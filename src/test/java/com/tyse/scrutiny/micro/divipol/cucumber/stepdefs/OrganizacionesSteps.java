package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.security.jwt.JwtAuthenticationTestUtils;
import com.tyse.scrutiny.micro.divipol.service.api.dto.OrganizacionPoliticaDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.OrganizacionPoliticaPage;
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
 * Step definitions para escenarios de organizaciones políticas.
 */
public class OrganizacionesSteps extends StepDefs {

    private static final String ORGANIZACIONES_API = "/api/organizaciones";

    @Autowired
    protected WebTestClient webTestClient;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    protected String jwtKey;

    // Estado compartido dentro de un escenario
    private OrganizacionPoliticaDTO lastCreatedOrganizacion;
    private OrganizacionPoliticaPage lastOrganizacionPage;

    // =====================================================
    // Helper: crear organización via API
    // =====================================================

    private OrganizacionPoliticaDTO createOrganizacionHelper(String nombre, String sigla, String tipo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", nombre);
        body.put("sigla", sigla != null ? sigla : "BDD");
        body.put("tipo", tipo != null ? tipo : "PARTIDO");

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        return webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .post()
            .uri(ORGANIZACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(OrganizacionPoliticaDTO.class)
            .returnResult()
            .getResponseBody();
    }

    // =====================================================
    // Steps: Precondiciones (Dado)
    // =====================================================

    @Dado("que existe una organización política {string}")
    public void queExisteUnaOrganizacionPolitica(String nombre) {
        lastCreatedOrganizacion = createOrganizacionHelper(nombre, "BDD", "PARTIDO");
        sharedOrganizacion = lastCreatedOrganizacion; // Compartir con otros steps
        assertThat(lastCreatedOrganizacion).isNotNull();
        assertThat(lastCreatedOrganizacion.getId()).isNotNull();
    }

    // =====================================================
    // Steps: Acciones (Cuando)
    // =====================================================

    @Cuando("creo una organización política con los datos:")
    public void creoUnaOrganizacionPoliticaConLosDatos(io.cucumber.datatable.DataTable dataTable) {
        lastCreatedOrganizacion = null;

        Map<String, String> datos = dataTable.asMap(String.class, String.class);
        Map<String, Object> body = new LinkedHashMap<>();
        datos.forEach(body::put);

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .post()
            .uri(ORGANIZACIONES_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();

        // Intentar capturar la organización creada
        try {
            lastCreatedOrganizacion = actions.expectBody(OrganizacionPoliticaDTO.class).returnResult().getResponseBody();
        } catch (AssertionError e) {
            lastCreatedOrganizacion = null;
        }
    }

    @Cuando("consulto la organización por su ID")
    public void consultoLaOrganizacionPorSuID() {
        assertThat(lastCreatedOrganizacion).as("Debe existir una organización previamente creada").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(ORGANIZACIONES_API + "/{id}", lastCreatedOrganizacion.getId())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    @Cuando("consulto la organización con ID {long}")
    public void consultoLaOrganizacionConID(long id) {
        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(ORGANIZACIONES_API + "/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    @Cuando("actualizo la organización con los datos:")
    public void actualizoLaOrganizacionConLosDatos(io.cucumber.datatable.DataTable dataTable) {
        assertThat(lastCreatedOrganizacion).as("Debe existir una organización previamente creada").isNotNull();

        Map<String, String> datos = dataTable.asMap(String.class, String.class);
        Map<String, Object> body = new LinkedHashMap<>();
        datos.forEach(body::put);

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .put()
            .uri(ORGANIZACIONES_API + "/{id}", lastCreatedOrganizacion.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(body)
            .exchange();

        // Intentar capturar la organización actualizada
        try {
            lastCreatedOrganizacion = actions.expectBody(OrganizacionPoliticaDTO.class).returnResult().getResponseBody();
        } catch (AssertionError e) {
            // Puede fallar si el update no es exitoso
        }
    }

    @Cuando("elimino la organización")
    public void eliminoLaOrganizacion() {
        assertThat(lastCreatedOrganizacion).as("Debe existir una organización previamente creada").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        actions = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .delete()
            .uri(ORGANIZACIONES_API + "/{id}", lastCreatedOrganizacion.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange();
    }

    // =====================================================
    // Steps: Validaciones (Y/Entonces)
    // =====================================================

    @Y("la organización creada tiene nombre {string}")
    public void laOrganizacionCreadaTieneNombre(String nombre) {
        if (lastCreatedOrganizacion == null) {
            lastCreatedOrganizacion = actions.expectBody(OrganizacionPoliticaDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedOrganizacion).isNotNull();
        assertThat(lastCreatedOrganizacion.getNombre()).isEqualTo(nombre);
    }

    @Y("la organización creada tiene sigla {string}")
    public void laOrganizacionCreadaTieneSigla(String sigla) {
        if (lastCreatedOrganizacion == null) {
            lastCreatedOrganizacion = actions.expectBody(OrganizacionPoliticaDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedOrganizacion).isNotNull();
        assertThat(lastCreatedOrganizacion.getSigla()).isEqualTo(sigla);
    }

    @Y("la organización creada tiene tipo {string}")
    public void laOrganizacionCreadaTieneTipo(String tipo) {
        if (lastCreatedOrganizacion == null) {
            lastCreatedOrganizacion = actions.expectBody(OrganizacionPoliticaDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedOrganizacion).isNotNull();
        assertThat(lastCreatedOrganizacion.getTipo().toString()).isEqualTo(tipo);
    }

    @Y("la organización creada está activa")
    public void laOrganizacionCreadaEstaActiva() {
        if (lastCreatedOrganizacion == null) {
            lastCreatedOrganizacion = actions.expectBody(OrganizacionPoliticaDTO.class).returnResult().getResponseBody();
        }
        assertThat(lastCreatedOrganizacion).isNotNull();
        assertThat(lastCreatedOrganizacion.getActivo()).isTrue();
    }

    @Y("la organización tiene nombre {string}")
    public void laOrganizacionTieneNombre(String nombre) {
        OrganizacionPoliticaDTO org = actions.expectBody(OrganizacionPoliticaDTO.class).returnResult().getResponseBody();

        assertThat(org).isNotNull();
        assertThat(org.getNombre()).isEqualTo(nombre);
    }

    @Y("la respuesta contiene una lista paginada de organizaciones")
    public void laRespuestaContieneUnaListaPaginadaDeOrganizaciones() {
        lastOrganizacionPage = actions.expectBody(OrganizacionPoliticaPage.class).returnResult().getResponseBody();

        assertThat(lastOrganizacionPage).isNotNull();
        assertThat(lastOrganizacionPage.getContent()).isNotNull();
        assertThat(lastOrganizacionPage.getTotalElements()).isGreaterThanOrEqualTo(1L);
    }

    @Y("al consultar la organización eliminada recibo {int}")
    public void alConsultarLaOrganizacionEliminadaRecibo(int statusCode) {
        assertThat(lastCreatedOrganizacion).as("Debe existir una organización previamente creada").isNotNull();

        String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);

        webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(ORGANIZACIONES_API + "/{id}", lastCreatedOrganizacion.getId())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus()
            .isEqualTo(statusCode);
    }
}

package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.pdf.PdfReader;
import com.tyse.scrutiny.micro.divipol.security.jwt.JwtAuthenticationTestUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

/**
 * Step definitions para tests de exportación DIVIPOL.
 *
 * Esta clase define los steps específicos para exportar datos
 * a CSV y PDF, incluyendo validaciones de contenido.
 */
public class DivipolExportacionSteps extends StepDefs {

    @Autowired
    protected WebTestClient webTestClient;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    protected String jwtKey;

    // Nota: 'authenticated', 'responseBytes', 'responseContentType'
    // se heredan de StepDefs (estáticos, compartidos)

    // ===== STEPS PARA EXPORTACIÓN CSV =====

    @Cuando("exporto a CSV el endpoint {string}")
    public void exportoACsvElEndpoint(String endpoint) {
        executeExportRequest(endpoint, "text/csv", null);
    }

    @Cuando("exporto a CSV el endpoint {string} con parámetros:")
    public void exportoACsvConParametros(String endpoint, DataTable params) {
        Map<String, String> parametros = convertDataTableToMap(params);
        executeExportRequest(endpoint, "text/csv", parametros);
    }

    @Cuando("exporto búsqueda a CSV con:")
    public void exportoBusquedaACsvCon(DataTable params) {
        Map<String, String> parametros = convertDataTableToMap(params);
        executeExportRequest("/api/divipol/export/search/csv", "text/csv", parametros);
    }

    // ===== STEPS PARA EXPORTACIÓN PDF =====

    @Cuando("exporto a PDF el endpoint {string}")
    public void exportoAPdfElEndpoint(String endpoint) {
        executeExportRequest(endpoint, "application/pdf", null);
    }

    @Cuando("exporto a PDF el endpoint {string} con parámetros:")
    public void exportoAPdfConParametros(String endpoint, DataTable params) {
        Map<String, String> parametros = convertDataTableToMap(params);
        executeExportRequest(endpoint, "application/pdf", parametros);
    }

    @Cuando("exporto búsqueda a PDF con:")
    public void exportoBusquedaAPdfCon(DataTable params) {
        Map<String, String> parametros = convertDataTableToMap(params);
        executeExportRequest("/api/divipol/export/search/pdf", "application/pdf", parametros);
    }

    // ===== VALIDACIONES DE CSV =====
    // Nota: El step "el content-type de la respuesta es {string}"
    // está definido en DivipolDepartamentosSteps y usa actions.expectHeader().contentType()

    @Entonces("el archivo CSV contiene el texto {string}")
    public void elCsvContieneTexto(String texto) {
        assertThat(responseBytes).isNotNull();
        String csv = new String(responseBytes, StandardCharsets.UTF_8);
        assertThat(csv).contains(texto);
    }

    @Entonces("el archivo CSV contiene alguno de los textos:")
    public void elCsvContieneAlgunoDeLosTextos(DataTable dataTable) {
        assertThat(responseBytes).isNotNull();
        String csv = new String(responseBytes, StandardCharsets.UTF_8);
        List<String> textos = dataTable.column(0);
        // Ignorar el header
        textos = textos.subList(1, textos.size());
        assertThat(csv).containsAnyOf(textos.toArray(new String[0]));
    }

    @Entonces("el archivo CSV contiene líneas que empiezan con {string}")
    public void elCsvContieneLineasQueEmpiezanCon(String prefijo) {
        assertThat(responseBytes).isNotNull();
        String csv = new String(responseBytes, StandardCharsets.UTF_8);
        boolean encontrado = csv
            .lines()
            .filter(line -> !line.startsWith("#") && !line.isEmpty())
            .anyMatch(line -> line.startsWith(prefijo) || line.contains("," + prefijo));
        assertThat(encontrado).as("El CSV debe contener líneas con códigos que empiecen con '%s'", prefijo).isTrue();
    }

    @Entonces("el archivo CSV contiene códigos de 9 dígitos")
    public void elCsvContieneCodigosDe9Digitos() {
        assertThat(responseBytes).isNotNull();
        String csv = new String(responseBytes, StandardCharsets.UTF_8);
        // Patrón para códigos de departamento: XX0000000 (9 dígitos)
        assertThat(csv).containsPattern("\\d{2}0000000");
    }

    @Entonces("el archivo CSV tiene máximo {int} líneas de datos")
    public void elCsvTieneMaximoLineasDeDatos(int maxLineas) {
        assertThat(responseBytes).isNotNull();
        String csv = new String(responseBytes, StandardCharsets.UTF_8);
        // Contar líneas de datos (excluyendo comentarios, vacías y header)
        long dataLines = csv.lines().filter(line -> !line.startsWith("#") && !line.isEmpty() && !line.startsWith("Código")).count();
        assertThat(dataLines)
            .as("El CSV debe tener máximo %d líneas de datos, pero tiene %d", maxLineas, dataLines)
            .isLessThanOrEqualTo(maxLineas);
    }

    // ===== VALIDACIONES DE PDF =====

    @Entonces("el archivo PDF es válido")
    public void elPdfEsValido() throws Exception {
        assertThat(responseBytes).isNotNull();
        assertThat(responseBytes.length).isGreaterThan(0);

        // Verificar que es un PDF válido usando OpenPDF
        PdfReader reader = new PdfReader(responseBytes);
        assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
        reader.close();
    }

    @Entonces("el archivo PDF tiene al menos {int} página\\(s)")
    public void elPdfTieneAlMenosPaginas(int paginas) throws Exception {
        assertThat(responseBytes).isNotNull();

        PdfReader reader = new PdfReader(responseBytes);
        assertThat(reader.getNumberOfPages()).as("El PDF debe tener al menos %d página(s)", paginas).isGreaterThanOrEqualTo(paginas);
        reader.close();
    }

    // Alternativa sin paréntesis
    @Entonces("el archivo PDF tiene al menos {int} página")
    public void elPdfTieneAlMenosPaginaSingular(int paginas) throws Exception {
        elPdfTieneAlMenosPaginas(paginas);
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Ejecuta una petición de exportación con el Accept header correspondiente.
     */
    private void executeExportRequest(String endpoint, String acceptHeader, Map<String, String> params) {
        WebTestClient.RequestHeadersSpec<?> request = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(30))
            .build()
            .get()
            .uri(uriBuilder -> buildUri(uriBuilder, endpoint, params))
            .accept(MediaType.parseMediaType(acceptHeader));

        if (authenticated) {
            String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        WebTestClient.ResponseSpec response = request.exchange();

        // Guardar respuesta para validaciones
        actions = response;

        // Intentar obtener los bytes de respuesta
        try {
            responseBytes = response.expectBody(byte[].class).returnResult().getResponseBody();
            responseContentType = response.returnResult(byte[].class).getResponseHeaders().getContentType() != null
                ? response.returnResult(byte[].class).getResponseHeaders().getContentType().toString()
                : "";
        } catch (AssertionError e) {
            // Si la respuesta no es exitosa (401, etc), no hay bytes
            responseBytes = null;
            responseContentType = "";
        }
    }

    /**
     * Construye la URI con los parámetros de query.
     */
    private java.net.URI buildUri(UriBuilder builder, String endpoint, Map<String, String> params) {
        builder.path(endpoint);
        if (params != null) {
            params.forEach((key, value) -> builder.queryParam(key, value));
        }
        return builder.build();
    }

    /**
     * Convierte un DataTable de Cucumber a un Map.
     * Espera formato: | parámetro | valor |
     */
    private Map<String, String> convertDataTableToMap(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        java.util.HashMap<String, String> result = new java.util.HashMap<>();
        for (Map<String, String> row : rows) {
            String param = row.get("parámetro");
            String value = row.get("valor");
            if (param != null && value != null) {
                result.put(param, value);
            }
        }
        return result;
    }
}

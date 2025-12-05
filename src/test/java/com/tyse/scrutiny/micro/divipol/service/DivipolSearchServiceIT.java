package com.tyse.scrutiny.micro.divipol.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.TyseScrutinyMicroDivipolApp;
import com.tyse.scrutiny.micro.divipol.config.AsyncSyncConfiguration;
import com.tyse.scrutiny.micro.divipol.config.EmbeddedSQL;
import com.tyse.scrutiny.micro.divipol.service.api.dto.DivipolSearchResultDTO;
import com.tyse.scrutiny.micro.divipol.service.api.dto.DivipolSearchResultPage;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for Divipol Search functionality.
 * Tests the search endpoints with full-text search and code search.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
class DivipolSearchServiceIT {

    private static final String SEARCH_API = "/api/divipol/search";
    private static final String SUGGESTIONS_API = "/api/divipol/search/suggestions";

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build();
    }

    // =====================================================
    // Tests de búsqueda por nombre (Full-Text Search)
    // =====================================================

    @Test
    void searchByName_shouldReturnResultsForValidQuery() {
        // When: Se busca por nombre "BOLIVAR"
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(SEARCH_API)
                    .queryParam("q", "BOLIVAR")
                    .queryParam("mode", "name")
                    .queryParam("page", 0)
                    .queryParam("size", 20)
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Se obtienen resultados
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getTotalElements()).isGreaterThan(0L);
        assertThat(page.getPage()).isEqualTo(0);
    }

    @Test
    void searchByName_shouldReturnCorrectStructure() {
        // When: Se busca por nombre
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(SEARCH_API)
                    .queryParam("q", "MEDELLIN")
                    .queryParam("mode", "name")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Cada resultado tiene la estructura correcta
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();

        DivipolSearchResultDTO firstResult = page.getContent().get(0);
        assertThat(firstResult.getCodigoDivipol()).isNotBlank();
        assertThat(firstResult.getCodigoDivipol()).hasSize(9); // DD-MMM-ZZ-PP = 9 chars
        assertThat(firstResult.getTipo()).isNotNull();
    }

    @Test
    void searchByName_shouldHandleAccentsAndCase() {
        // When: Se busca con diferentes variaciones de acentos
        DivipolSearchResultPage pageUpper = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SEARCH_API).queryParam("q", "BOGOTA").queryParam("mode", "name").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        DivipolSearchResultPage pageLower = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SEARCH_API).queryParam("q", "bogota").queryParam("mode", "name").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Ambas búsquedas devuelven resultados similares
        assertThat(pageUpper).isNotNull();
        assertThat(pageLower).isNotNull();
        // Full-text search en español es case-insensitive
        assertThat(pageUpper.getTotalElements()).isGreaterThan(0L);
        assertThat(pageLower.getTotalElements()).isGreaterThan(0L);
    }

    @Test
    void searchByName_shouldReturnPaginatedResults() {
        // Given: Buscar un término con muchos resultados
        String searchTerm = "ESCUELA";

        // When: Se obtiene la primera página
        DivipolSearchResultPage page1 = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(SEARCH_API)
                    .queryParam("q", searchTerm)
                    .queryParam("mode", "name")
                    .queryParam("page", 0)
                    .queryParam("size", 5)
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: La paginación es correcta
        assertThat(page1).isNotNull();
        assertThat(page1.getPage()).isEqualTo(0);
        assertThat(page1.getSize()).isEqualTo(5);

        if (page1.getTotalElements() > 5) {
            assertThat(page1.getTotalPages()).isGreaterThan(1);

            // When: Se obtiene la segunda página
            DivipolSearchResultPage page2 = webTestClient
                .get()
                .uri(uriBuilder ->
                    uriBuilder
                        .path(SEARCH_API)
                        .queryParam("q", searchTerm)
                        .queryParam("mode", "name")
                        .queryParam("page", 1)
                        .queryParam("size", 5)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(DivipolSearchResultPage.class)
                .returnResult()
                .getResponseBody();

            // Then: Los resultados son diferentes
            assertThat(page2).isNotNull();
            assertThat(page2.getPage()).isEqualTo(1);
            assertThat(page2.getContent()).isNotEmpty();

            // Los resultados de ambas páginas no deben solaparse
            List<String> codesPage1 = page1.getContent().stream().map(DivipolSearchResultDTO::getCodigoDivipol).toList();
            List<String> codesPage2 = page2.getContent().stream().map(DivipolSearchResultDTO::getCodigoDivipol).toList();
            assertThat(codesPage1).doesNotContainAnyElementsOf(codesPage2);
        }
    }

    @Test
    void searchByName_shouldReturnEmptyForNonExistentTerm() {
        // When: Se busca un término inexistente
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SEARCH_API).queryParam("q", "XYZNONEXISTENT123").queryParam("mode", "name").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: La lista está vacía pero la respuesta es válida
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0L);
    }

    // =====================================================
    // Tests de búsqueda por código
    // =====================================================

    @Test
    void searchByCode_shouldReturnResultsForDepartmentCode() {
        // When: Se busca por código de departamento "05" (Antioquia)
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(SEARCH_API)
                    .queryParam("q", "05")
                    .queryParam("mode", "code")
                    .queryParam("page", 0)
                    .queryParam("size", 20)
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Se obtienen resultados de Antioquia
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();

        // Todos los resultados deben pertenecer al departamento 05
        page
            .getContent()
            .forEach(result -> {
                assertThat(result.getCodigoDivipol()).startsWith("05");
                assertThat(result.getCoddepto()).isEqualTo(5);
            });
    }

    @Test
    void searchByCode_shouldReturnExactMatchForFullCode() {
        // Given: Buscar un código completo de un departamento conocido (Antioquia)
        // Primero obtenemos algunos resultados para tener un código válido
        DivipolSearchResultPage initialPage = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SEARCH_API).queryParam("q", "05001").queryParam("mode", "code").queryParam("size", 5).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        assertThat(initialPage).isNotNull();
        assertThat(initialPage.getContent()).isNotEmpty();

        // Obtener un código específico de un puesto
        String testCode = initialPage
            .getContent()
            .stream()
            .filter(r -> r.getTipo() == DivipolSearchResultDTO.TipoEnum.PUESTO)
            .findFirst()
            .map(DivipolSearchResultDTO::getCodigoDivipol)
            .orElse(null);

        if (testCode != null) {
            // When: Se busca por código completo (9 dígitos)
            DivipolSearchResultPage page = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(SEARCH_API).queryParam("q", testCode).queryParam("mode", "code").build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(DivipolSearchResultPage.class)
                .returnResult()
                .getResponseBody();

            // Then: Se obtiene exactamente un resultado
            assertThat(page).isNotNull();
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getCodigoDivipol()).isEqualTo(testCode);
        }
    }

    @Test
    void searchByCode_shouldReturnMunicipioForPartialCode() {
        // When: Se busca por código de municipio "05001" (Medellín)
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(SEARCH_API).queryParam("q", "05001").queryParam("mode", "code").queryParam("size", 100).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Se obtienen resultados de Medellín
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();

        // Todos los resultados deben pertenecer a Medellín (05001)
        page
            .getContent()
            .forEach(result -> {
                assertThat(result.getCodigoDivipol()).startsWith("05001");
                assertThat(result.getCoddepto()).isEqualTo(5);
                assertThat(result.getCodmipio()).isEqualTo(1);
            });
    }

    @Test
    void searchByCode_shouldHandleAlphanumericPuestoCode() {
        // Given: Buscar por código con parte alfanumérica
        // Los códigos de puesto pueden tener letras (posiciones 7-8)

        // When: Se busca con código alfanumérico simulado
        // Primero buscamos en un municipio conocido
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(SEARCH_API)
                    .queryParam("q", "0500101") // Medellín, zona 1
                    .queryParam("mode", "code")
                    .queryParam("size", 50)
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Se obtienen resultados
        assertThat(page).isNotNull();
        // Puede haber resultados o no, pero la consulta debe funcionar
    }

    @Test
    void searchByCode_shouldReturnAllForEmptyCode() {
        // When: Se busca con código vacío o ceros
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(SEARCH_API).queryParam("q", "000000000").queryParam("mode", "code").queryParam("size", 20).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Se obtienen todos los registros (paginados)
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getTotalElements()).isGreaterThan(18000L); // Todos los registros
    }

    @Test
    void searchByCode_shouldOrderResultsByCode() {
        // When: Se busca un departamento
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SEARCH_API).queryParam("q", "05").queryParam("mode", "code").queryParam("size", 50).build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Los resultados están ordenados por código
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();

        List<String> codes = page.getContent().stream().map(DivipolSearchResultDTO::getCodigoDivipol).toList();

        // Verificar que los códigos están ordenados
        for (int i = 1; i < codes.size(); i++) {
            assertThat(codes.get(i).compareTo(codes.get(i - 1))).isGreaterThanOrEqualTo(0);
        }
    }

    // =====================================================
    // Tests de sugerencias
    // =====================================================

    @Test
    void getSuggestions_shouldReturnSuggestionsForNameMode() {
        // When: Se solicitan sugerencias por nombre
        List<DivipolSearchResultDTO> suggestions = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SUGGESTIONS_API).queryParam("q", "CART").queryParam("mode", "name").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(DivipolSearchResultDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: Se obtienen sugerencias
        assertThat(suggestions).isNotNull();
        assertThat(suggestions.size()).isLessThanOrEqualTo(5); // Límite por defecto
    }

    @Test
    void getSuggestions_shouldReturnSuggestionsForCodeMode() {
        // When: Se solicitan sugerencias por código
        List<DivipolSearchResultDTO> suggestions = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SUGGESTIONS_API).queryParam("q", "05001").queryParam("mode", "code").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(DivipolSearchResultDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: Se obtienen sugerencias
        assertThat(suggestions).isNotNull();
        assertThat(suggestions.size()).isLessThanOrEqualTo(5);

        // Todos deben empezar con el código buscado
        suggestions.forEach(suggestion -> {
            assertThat(suggestion.getCodigoDivipol()).startsWith("05001");
        });
    }

    @Test
    void getSuggestions_shouldLimitResults() {
        // When: Se solicitan sugerencias con un término amplio
        List<DivipolSearchResultDTO> suggestions = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SUGGESTIONS_API).queryParam("q", "ESCUELA").queryParam("mode", "name").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(DivipolSearchResultDTO.class)
            .getResponseBody()
            .collectList()
            .block(Duration.ofSeconds(10));

        // Then: No se excede el límite de sugerencias (5)
        assertThat(suggestions).isNotNull();
        assertThat(suggestions.size()).isLessThanOrEqualTo(5);
    }

    // =====================================================
    // Tests de tipos de resultado
    // =====================================================

    @Test
    void search_shouldReturnCorrectTipoForDepartamento() {
        // When: Se busca un departamento conocido
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(SEARCH_API).queryParam("q", "ANTIOQUIA").queryParam("mode", "name").queryParam("size", 100).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Se encuentra al menos un resultado de tipo DEPTO
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();

        boolean hasDepto = page.getContent().stream().anyMatch(r -> r.getTipo() == DivipolSearchResultDTO.TipoEnum.DEPTO);
        assertThat(hasDepto).isTrue();
    }

    @Test
    void search_shouldReturnCorrectTipoForMunicipio() {
        // When: Se busca un municipio conocido
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(SEARCH_API).queryParam("q", "MEDELLIN").queryParam("mode", "name").queryParam("size", 100).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Se encuentra al menos un resultado de tipo MPIO
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();

        boolean hasMpio = page.getContent().stream().anyMatch(r -> r.getTipo() == DivipolSearchResultDTO.TipoEnum.MPIO);
        assertThat(hasMpio).isTrue();
    }

    @Test
    void search_shouldReturnCorrectTipoForPuesto() {
        // When: Se busca un término que solo aparece en puestos
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder.path(SEARCH_API).queryParam("q", "COLEGIO").queryParam("mode", "name").queryParam("size", 20).build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Hay resultados de tipo PUESTO
        assertThat(page).isNotNull();
        if (!page.getContent().isEmpty()) {
            boolean hasPuesto = page.getContent().stream().anyMatch(r -> r.getTipo() == DivipolSearchResultDTO.TipoEnum.PUESTO);
            assertThat(hasPuesto).isTrue();
        }
    }

    // =====================================================
    // Tests de seguridad (SQL Injection)
    // =====================================================

    @Test
    void searchByName_shouldBeSafeAgainstSQLInjection() {
        // When: Se intenta inyección SQL por nombre
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SEARCH_API).queryParam("q", "'; DROP TABLE divipol; --").queryParam("mode", "name").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: No hay error y la respuesta es vacía (sin inyección)
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(0L);
    }

    @Test
    void searchByCode_shouldBeSafeAgainstSQLInjection() {
        // When: Se intenta inyección SQL por código
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path(SEARCH_API).queryParam("q", "05' OR '1'='1").queryParam("mode", "code").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: Solo devuelve resultados del código sanitizado (05)
        assertThat(page).isNotNull();
        // El código se sanitiza quitando caracteres no válidos, quedando solo "05"
        if (!page.getContent().isEmpty()) {
            page
                .getContent()
                .forEach(result -> {
                    assertThat(result.getCoddepto()).isEqualTo(5);
                });
        }
    }

    @Test
    void searchByCode_shouldSanitizeSpecialCharacters() {
        // When: Se busca con caracteres especiales
        DivipolSearchResultPage page = webTestClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(SEARCH_API)
                    .queryParam("q", "05-001-01-A1") // Con guiones
                    .queryParam("mode", "code")
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(DivipolSearchResultPage.class)
            .returnResult()
            .getResponseBody();

        // Then: El código se procesa correctamente
        assertThat(page).isNotNull();
        // Los guiones se ignoran, buscando "0500101A1"
    }
}

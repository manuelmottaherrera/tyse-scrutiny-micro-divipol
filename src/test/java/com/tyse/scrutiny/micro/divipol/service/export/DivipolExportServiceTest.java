package com.tyse.scrutiny.micro.divipol.service.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tyse.scrutiny.micro.divipol.repository.DivipolRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Tests unitarios para DivipolExportService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DivipolExportService - Lógica de exportación")
class DivipolExportServiceTest {

    @Mock
    private DivipolRepository divipolRepository;

    @Mock
    private DivipolPdfGenerator pdfGenerator;

    private DivipolExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new DivipolExportService(divipolRepository, pdfGenerator);
    }

    // =====================================================
    // Helpers para crear datos de prueba
    // =====================================================

    private DivipolStatsDTO createMockStats() {
        return new DivipolStatsDTO()
            .totalDepartamentos(33L)
            .totalMunicipios(1122L)
            .totalZonas(2500L)
            .totalPuestos(12000L)
            .totalMesas(115000L)
            .potencialFemenino(20000000L)
            .potencialMasculino(19000000L)
            .potencialTotal(39000000L);
    }

    private List<DivipolDepartamentoDTO> createMockDepartamentos() {
        return Arrays.asList(
            new DivipolDepartamentoDTO()
                .coddepto(5)
                .nomdepto("ANTIOQUIA")
                .mujeres(3500000L)
                .hombres(3300000L)
                .totalPotencial(6800000L)
                .mesas(25000L),
            new DivipolDepartamentoDTO()
                .coddepto(11)
                .nomdepto("BOGOTA D.C.")
                .mujeres(4200000L)
                .hombres(3900000L)
                .totalPotencial(8100000L)
                .mesas(30000L)
        );
    }

    private List<DivipolMunicipioDTO> createMockMunicipios() {
        return Arrays.asList(
            new DivipolMunicipioDTO()
                .coddepto(5)
                .codmipio(1)
                .nomdepto("ANTIOQUIA")
                .nommipio("MEDELLIN")
                .potencialFemenino(1300000L)
                .potencialMasculino(1200000L)
                .potencialTotal(2500000L)
                .mesas(10000L),
            new DivipolMunicipioDTO()
                .coddepto(5)
                .codmipio(2)
                .nomdepto("ANTIOQUIA")
                .nommipio("ABEJORRAL")
                .potencialFemenino(10000L)
                .potencialMasculino(9500L)
                .potencialTotal(19500L)
                .mesas(80L)
        );
    }

    private List<DivipolZonaDTO> createMockZonas() {
        return Arrays.asList(
            new DivipolZonaDTO()
                .coddepto(5)
                .codmipio(1)
                .codzona(1)
                .nomdepto("ANTIOQUIA")
                .nommipio("MEDELLIN")
                .potencialFemenino(50000L)
                .potencialMasculino(48000L)
                .potencialTotal(98000L)
                .mesas(400L)
        );
    }

    private List<DivipolPuestoDTO> createMockPuestos() {
        return Arrays.asList(
            new DivipolPuestoDTO()
                .coddepto(5)
                .codmipio(1)
                .codzona(1)
                .codpuesto("01")
                .nomdepto("ANTIOQUIA")
                .nommipio("MEDELLIN")
                .nompuesto("PUESTO CENTRO")
                .potencialFemenino(5000L)
                .potencialMasculino(4800L)
                .potencialTotal(9800L)
                .mesas(40L)
        );
    }

    private List<DivipolSearchResultDTO> createMockSearchResults() {
        return Arrays.asList(
            new DivipolSearchResultDTO()
                .codigoDivipol("050010000")
                .tipo(DivipolSearchResultDTO.TipoEnum.MPIO)
                .coddepto(5)
                .codmipio(1)
                .nomdepto("ANTIOQUIA")
                .nommipio("MEDELLIN")
                .potencialTotal(2500000L)
                .mesas(10000L)
        );
    }

    // =====================================================
    // Tests de exportFiltersToCsv
    // =====================================================

    @Nested
    @DisplayName("exportFiltersToCsv")
    class ExportFiltersToCsvTests {

        @Test
        @DisplayName("Debe generar CSV de departamentos cuando no hay filtros")
        void shouldGenerateDepartamentosCsvWhenNoFilters() {
            when(divipolRepository.findAllDepartamentos()).thenReturn(Flux.fromIterable(createMockDepartamentos()));
            when(divipolRepository.getGeneralStats()).thenReturn(Mono.just(createMockStats()));

            byte[] bytes = exportService.exportFiltersToCsv(null, null, null).block();

            assertThat(bytes).isNotEmpty();
            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("Reporte DIVIPOL");
            assertThat(csv).contains("ANTIOQUIA");
            assertThat(csv).contains("050000000"); // Código con formato 9 dígitos
            assertThat(csv).contains("110000000");

            verify(divipolRepository).findAllDepartamentos();
            verify(divipolRepository).getGeneralStats();
        }

        @Test
        @DisplayName("Debe generar CSV de municipios cuando hay codDepto")
        void shouldGenerateMunicipiosCsvWhenCodDeptoProvided() {
            when(divipolRepository.findMunicipiosByDepartamento(5)).thenReturn(Flux.fromIterable(createMockMunicipios()));
            when(divipolRepository.getStatsByDepartamento(5)).thenReturn(Mono.just(createMockStats()));

            byte[] bytes = exportService.exportFiltersToCsv(5, null, null).block();

            assertThat(bytes).isNotEmpty();
            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("MEDELLIN");
            assertThat(csv).contains("050010000"); // Código municipio con formato

            verify(divipolRepository).findMunicipiosByDepartamento(5);
            verify(divipolRepository).getStatsByDepartamento(5);
        }

        @Test
        @DisplayName("Debe generar CSV de zonas cuando hay codDepto y codMpio")
        void shouldGenerateZonasCsvWhenCodDeptoAndCodMpioProvided() {
            when(divipolRepository.findZonasByMunicipio(5, 1)).thenReturn(Flux.fromIterable(createMockZonas()));
            when(divipolRepository.getStatsByMunicipio(5, 1)).thenReturn(Mono.just(createMockStats()));

            byte[] bytes = exportService.exportFiltersToCsv(5, 1, null).block();

            assertThat(bytes).isNotEmpty();
            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("Zona 1");
            assertThat(csv).contains("050010100"); // Código zona con formato

            verify(divipolRepository).findZonasByMunicipio(5, 1);
            verify(divipolRepository).getStatsByMunicipio(5, 1);
        }

        @Test
        @DisplayName("Debe generar CSV de puestos cuando hay codDepto, codMpio y codZona")
        void shouldGeneratePuestosCsvWhenAllFiltersProvided() {
            when(divipolRepository.findPuestosByZona(5, 1, 1)).thenReturn(Flux.fromIterable(createMockPuestos()));
            when(divipolRepository.getStatsByZona(5, 1, 1)).thenReturn(Mono.just(createMockStats()));

            byte[] bytes = exportService.exportFiltersToCsv(5, 1, 1).block();

            assertThat(bytes).isNotEmpty();
            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("PUESTO CENTRO");
            assertThat(csv).contains("05001"); // Parte del código puesto

            verify(divipolRepository).findPuestosByZona(5, 1, 1);
            verify(divipolRepository).getStatsByZona(5, 1, 1);
        }

        @Test
        @DisplayName("Debe incluir header con estadísticas en CSV")
        void shouldIncludeStatsHeaderInCsv() {
            when(divipolRepository.findAllDepartamentos()).thenReturn(Flux.fromIterable(createMockDepartamentos()));
            when(divipolRepository.getGeneralStats()).thenReturn(Mono.just(createMockStats()));

            byte[] bytes = exportService.exportFiltersToCsv(null, null, null).block();

            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("# ESTADÍSTICAS");
            assertThat(csv).contains("# Departamentos: 33");
            assertThat(csv).contains("# Municipios: 1122");
            assertThat(csv).contains("# Potencial Total: 39000000");
        }

        @Test
        @DisplayName("Debe escapar caracteres especiales en CSV")
        void shouldEscapeSpecialCharactersInCsv() {
            DivipolDepartamentoDTO deptoWithComma = new DivipolDepartamentoDTO()
                .coddepto(99)
                .nomdepto("DEPTO, CON COMA")
                .mujeres(1000L)
                .hombres(900L)
                .totalPotencial(1900L)
                .mesas(10L);

            when(divipolRepository.findAllDepartamentos()).thenReturn(Flux.just(deptoWithComma));
            when(divipolRepository.getGeneralStats()).thenReturn(Mono.just(createMockStats()));

            byte[] bytes = exportService.exportFiltersToCsv(null, null, null).block();

            String csv = new String(bytes, StandardCharsets.UTF_8);
            // Los valores con comas deben estar entre comillas
            assertThat(csv).contains("\"DEPTO, CON COMA\"");
        }
    }

    // =====================================================
    // Tests de exportFiltersToPdf
    // =====================================================

    @Nested
    @DisplayName("exportFiltersToPdf")
    class ExportFiltersToPdfTests {

        @Test
        @DisplayName("Debe llamar a pdfGenerator para departamentos")
        void shouldCallPdfGeneratorForDepartamentos() {
            List<DivipolDepartamentoDTO> deptos = createMockDepartamentos();
            DivipolStatsDTO stats = createMockStats();
            byte[] mockPdf = "PDF_CONTENT".getBytes();

            when(divipolRepository.findAllDepartamentos()).thenReturn(Flux.fromIterable(deptos));
            when(divipolRepository.getGeneralStats()).thenReturn(Mono.just(stats));
            when(pdfGenerator.generateDepartamentosReport(anyList(), any())).thenReturn(mockPdf);

            byte[] bytes = exportService.exportFiltersToPdf(null, null, null).block();

            assertThat(bytes).isEqualTo(mockPdf);
            verify(pdfGenerator).generateDepartamentosReport(anyList(), any());
        }

        @Test
        @DisplayName("Debe llamar a pdfGenerator para municipios")
        void shouldCallPdfGeneratorForMunicipios() {
            List<DivipolMunicipioDTO> mpios = createMockMunicipios();
            DivipolStatsDTO stats = createMockStats();
            byte[] mockPdf = "PDF_CONTENT".getBytes();

            when(divipolRepository.findMunicipiosByDepartamento(5)).thenReturn(Flux.fromIterable(mpios));
            when(divipolRepository.getStatsByDepartamento(5)).thenReturn(Mono.just(stats));
            when(pdfGenerator.generateMunicipiosReport(anyList(), any(), eq("ANTIOQUIA"))).thenReturn(mockPdf);

            byte[] bytes = exportService.exportFiltersToPdf(5, null, null).block();

            assertThat(bytes).isEqualTo(mockPdf);
            verify(pdfGenerator).generateMunicipiosReport(anyList(), any(), eq("ANTIOQUIA"));
        }
    }

    // =====================================================
    // Tests de exportSearchToCsv
    // =====================================================

    @Nested
    @DisplayName("exportSearchToCsv")
    class ExportSearchToCsvTests {

        @Test
        @DisplayName("Debe exportar página actual cuando exportAll=false")
        void shouldExportCurrentPageWhenExportAllFalse() {
            when(divipolRepository.searchByName("MEDELLIN", 0, 20)).thenReturn(Flux.fromIterable(createMockSearchResults()));
            when(divipolRepository.countSearchByName("MEDELLIN")).thenReturn(Mono.just(1L));

            byte[] bytes = exportService.exportSearchToCsv("MEDELLIN", "name", 0, 20, false).block();

            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("Reporte de Búsqueda DIVIPOL");
            assertThat(csv).contains("MEDELLIN");
            assertThat(csv).contains("Modo: Nombre");

            verify(divipolRepository).searchByName("MEDELLIN", 0, 20);
        }

        @Test
        @DisplayName("Debe exportar todos los resultados cuando exportAll=true")
        void shouldExportAllResultsWhenExportAllTrue() {
            when(divipolRepository.searchByName("ANTIOQUIA", 0, 10000)).thenReturn(Flux.fromIterable(createMockSearchResults()));
            when(divipolRepository.countSearchByName("ANTIOQUIA")).thenReturn(Mono.just(500L));

            byte[] bytes = exportService.exportSearchToCsv("ANTIOQUIA", "name", 0, 20, true).block();

            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("Total resultados: 500");

            // Debe buscar con tamaño máximo (10000) cuando exportAll=true
            verify(divipolRepository).searchByName("ANTIOQUIA", 0, 10000);
        }

        @Test
        @DisplayName("Debe usar búsqueda por código cuando mode=code")
        void shouldUseSearchByCodeWhenModeIsCode() {
            when(divipolRepository.searchByCode("050010000", 0, 20)).thenReturn(Flux.fromIterable(createMockSearchResults()));
            when(divipolRepository.countSearchByCode("050010000")).thenReturn(Mono.just(1L));

            byte[] bytes = exportService.exportSearchToCsv("050010000", "code", 0, 20, false).block();

            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("Modo: Código");

            verify(divipolRepository).searchByCode("050010000", 0, 20);
            verify(divipolRepository).countSearchByCode("050010000");
        }

        @Test
        @DisplayName("Debe usar modo name por defecto cuando mode es null")
        void shouldDefaultToNameModeWhenModeIsNull() {
            when(divipolRepository.searchByName("BOGOTA", 0, 20)).thenReturn(Flux.fromIterable(createMockSearchResults()));
            when(divipolRepository.countSearchByName("BOGOTA")).thenReturn(Mono.just(1L));

            byte[] bytes = exportService.exportSearchToCsv("BOGOTA", null, 0, 20, false).block();

            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertThat(csv).contains("Modo: Nombre");

            verify(divipolRepository).searchByName("BOGOTA", 0, 20);
        }
    }

    // =====================================================
    // Tests de exportSearchToPdf
    // =====================================================

    @Nested
    @DisplayName("exportSearchToPdf")
    class ExportSearchToPdfTests {

        @Test
        @DisplayName("Debe llamar a pdfGenerator para búsqueda")
        void shouldCallPdfGeneratorForSearch() {
            List<DivipolSearchResultDTO> results = createMockSearchResults();
            byte[] mockPdf = "PDF_SEARCH".getBytes();

            when(divipolRepository.searchByName("MEDELLIN", 0, 20)).thenReturn(Flux.fromIterable(results));
            when(divipolRepository.countSearchByName("MEDELLIN")).thenReturn(Mono.just(1L));
            when(pdfGenerator.generateSearchReport(anyList(), eq("MEDELLIN"), eq("name"), eq(1L))).thenReturn(mockPdf);

            byte[] bytes = exportService.exportSearchToPdf("MEDELLIN", "name", 0, 20, false).block();

            assertThat(bytes).isEqualTo(mockPdf);
            verify(pdfGenerator).generateSearchReport(anyList(), eq("MEDELLIN"), eq("name"), eq(1L));
        }

        @Test
        @DisplayName("Debe exportar todos los resultados en PDF cuando exportAll=true")
        void shouldExportAllResultsInPdfWhenExportAllTrue() {
            List<DivipolSearchResultDTO> results = createMockSearchResults();
            byte[] mockPdf = "PDF_ALL".getBytes();

            when(divipolRepository.searchByName("ANTIOQUIA", 0, 10000)).thenReturn(Flux.fromIterable(results));
            when(divipolRepository.countSearchByName("ANTIOQUIA")).thenReturn(Mono.just(500L));
            when(pdfGenerator.generateSearchReport(anyList(), eq("ANTIOQUIA"), eq("name"), eq(500L))).thenReturn(mockPdf);

            byte[] bytes = exportService.exportSearchToPdf("ANTIOQUIA", "name", 0, 20, true).block();

            assertThat(bytes).isEqualTo(mockPdf);
            // Debe usar límite máximo cuando exportAll=true
            verify(divipolRepository).searchByName("ANTIOQUIA", 0, 10000);
        }
    }
}

package com.tyse.scrutiny.micro.divipol.service.export;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.pdf.PdfReader;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para DivipolPdfGenerator.
 */
@DisplayName("DivipolPdfGenerator - Generación de PDFs")
class DivipolPdfGeneratorTest {

    private DivipolPdfGenerator pdfGenerator;

    @BeforeEach
    void setUp() {
        pdfGenerator = new DivipolPdfGenerator();
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
                .mesas(10000L),
            new DivipolSearchResultDTO()
                .codigoDivipol("050000000")
                .tipo(DivipolSearchResultDTO.TipoEnum.DEPTO)
                .coddepto(5)
                .nomdepto("ANTIOQUIA")
                .potencialTotal(6800000L)
                .mesas(25000L)
        );
    }

    // =====================================================
    // Tests de generateDepartamentosReport
    // =====================================================

    @Nested
    @DisplayName("generateDepartamentosReport")
    class GenerateDepartamentosReportTests {

        @Test
        @DisplayName("Debe generar PDF válido con datos de departamentos")
        void shouldGenerateValidPdfWithDepartamentos() throws Exception {
            List<DivipolDepartamentoDTO> data = createMockDepartamentos();
            DivipolStatsDTO stats = createMockStats();

            byte[] pdfBytes = pdfGenerator.generateDepartamentosReport(data, stats);

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            // Verificar que es un PDF válido
            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }

        @Test
        @DisplayName("Debe manejar lista vacía de departamentos")
        void shouldHandleEmptyDepartamentosList() throws Exception {
            List<DivipolDepartamentoDTO> data = Collections.emptyList();
            DivipolStatsDTO stats = createMockStats();

            byte[] pdfBytes = pdfGenerator.generateDepartamentosReport(data, stats);

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }

        @Test
        @DisplayName("Debe manejar stats null")
        void shouldHandleNullStats() throws Exception {
            List<DivipolDepartamentoDTO> data = createMockDepartamentos();

            byte[] pdfBytes = pdfGenerator.generateDepartamentosReport(data, null);

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);
        }
    }

    // =====================================================
    // Tests de generateMunicipiosReport
    // =====================================================

    @Nested
    @DisplayName("generateMunicipiosReport")
    class GenerateMunicipiosReportTests {

        @Test
        @DisplayName("Debe generar PDF válido con municipios")
        void shouldGenerateValidPdfWithMunicipios() throws Exception {
            List<DivipolMunicipioDTO> data = Arrays.asList(
                new DivipolMunicipioDTO()
                    .coddepto(5)
                    .codmipio(1)
                    .nomdepto("ANTIOQUIA")
                    .nommipio("MEDELLIN")
                    .potencialFemenino(1300000L)
                    .potencialMasculino(1200000L)
                    .potencialTotal(2500000L)
                    .mesas(10000L)
            );
            DivipolStatsDTO stats = createMockStats();

            byte[] pdfBytes = pdfGenerator.generateMunicipiosReport(data, stats, "ANTIOQUIA");

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }
    }

    // =====================================================
    // Tests de generateZonasReport
    // =====================================================

    @Nested
    @DisplayName("generateZonasReport")
    class GenerateZonasReportTests {

        @Test
        @DisplayName("Debe generar PDF válido con zonas")
        void shouldGenerateValidPdfWithZonas() throws Exception {
            List<DivipolZonaDTO> data = Arrays.asList(
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
            DivipolStatsDTO stats = createMockStats();

            byte[] pdfBytes = pdfGenerator.generateZonasReport(data, stats, "ANTIOQUIA", "MEDELLIN");

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }
    }

    // =====================================================
    // Tests de generatePuestosReport
    // =====================================================

    @Nested
    @DisplayName("generatePuestosReport")
    class GeneratePuestosReportTests {

        @Test
        @DisplayName("Debe generar PDF válido con puestos")
        void shouldGenerateValidPdfWithPuestos() throws Exception {
            List<DivipolPuestoDTO> data = Arrays.asList(
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
            DivipolStatsDTO stats = createMockStats();

            byte[] pdfBytes = pdfGenerator.generatePuestosReport(data, stats, "ANTIOQUIA", "MEDELLIN", 1);

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }
    }

    // =====================================================
    // Tests de generateSearchReport
    // =====================================================

    @Nested
    @DisplayName("generateSearchReport")
    class GenerateSearchReportTests {

        @Test
        @DisplayName("Debe generar PDF válido con resultados de búsqueda")
        void shouldGenerateValidPdfWithSearchResults() throws Exception {
            List<DivipolSearchResultDTO> results = createMockSearchResults();

            byte[] pdfBytes = pdfGenerator.generateSearchReport(results, "MEDELLIN", "name", 2L);

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }

        @Test
        @DisplayName("Debe manejar búsqueda por código")
        void shouldHandleSearchByCode() throws Exception {
            List<DivipolSearchResultDTO> results = createMockSearchResults();

            byte[] pdfBytes = pdfGenerator.generateSearchReport(results, "050010000", "code", 1L);

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }

        @Test
        @DisplayName("Debe manejar lista vacía de resultados")
        void shouldHandleEmptySearchResults() throws Exception {
            List<DivipolSearchResultDTO> results = Collections.emptyList();

            byte[] pdfBytes = pdfGenerator.generateSearchReport(results, "NORESULTS", "name", 0L);

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }

        @Test
        @DisplayName("Debe manejar diferentes tipos de resultados")
        void shouldHandleDifferentResultTypes() throws Exception {
            List<DivipolSearchResultDTO> results = Arrays.asList(
                new DivipolSearchResultDTO()
                    .codigoDivipol("050000000")
                    .tipo(DivipolSearchResultDTO.TipoEnum.DEPTO)
                    .coddepto(5)
                    .nomdepto("ANTIOQUIA")
                    .potencialTotal(6800000L)
                    .mesas(25000L),
                new DivipolSearchResultDTO()
                    .codigoDivipol("050010000")
                    .tipo(DivipolSearchResultDTO.TipoEnum.MPIO)
                    .coddepto(5)
                    .codmipio(1)
                    .nomdepto("ANTIOQUIA")
                    .nommipio("MEDELLIN")
                    .potencialTotal(2500000L)
                    .mesas(10000L),
                new DivipolSearchResultDTO()
                    .codigoDivipol("050010100")
                    .tipo(DivipolSearchResultDTO.TipoEnum.ZONA)
                    .coddepto(5)
                    .codmipio(1)
                    .codzona(1)
                    .nomdepto("ANTIOQUIA")
                    .nommipio("MEDELLIN")
                    .potencialTotal(98000L)
                    .mesas(400L),
                new DivipolSearchResultDTO()
                    .codigoDivipol("050010101")
                    .tipo(DivipolSearchResultDTO.TipoEnum.PUESTO)
                    .coddepto(5)
                    .codmipio(1)
                    .codzona(1)
                    .codpuesto("01")
                    .nomdepto("ANTIOQUIA")
                    .nommipio("MEDELLIN")
                    .nompuesto("PUESTO CENTRO")
                    .potencialTotal(9800L)
                    .mesas(40L)
            );

            byte[] pdfBytes = pdfGenerator.generateSearchReport(results, "ANTIOQUIA", "name", 4L);

            assertThat(pdfBytes).isNotNull();
            assertThat(pdfBytes.length).isGreaterThan(0);

            PdfReader reader = new PdfReader(pdfBytes);
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            reader.close();
        }
    }
}

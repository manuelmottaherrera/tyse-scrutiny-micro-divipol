package com.tyse.scrutiny.micro.divipol.service.export;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Generador de reportes PDF usando OpenPDF.
 * Usa fuentes Liberation Sans embebidas para compatibilidad en contenedores.
 */
@Component
public class DivipolPdfGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(DivipolPdfGenerator.class);

    // Colores (funcionan en modo headless)
    private static final Color HEADER_BG = new Color(52, 73, 94);
    private static final Color ROW_EVEN = new Color(245, 245, 245);
    private static final Color ROW_ODD = Color.WHITE;
    private static final Color STATS_BG = new Color(236, 240, 241);
    private static final Color TITLE_COLOR = new Color(51, 51, 51);
    private static final Color SUBTITLE_COLOR = new Color(102, 102, 102);
    private static final Color FOOTER_COLOR = new Color(128, 128, 128);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);

    // Fuentes inicializadas lazily para evitar problemas de carga
    private Font titleFont;
    private Font subtitleFont;
    private Font headerFont;
    private Font cellFont;
    private Font statsLabelFont;
    private Font statsValueFont;
    private Font footerFont;
    private volatile boolean fontsInitialized = false;

    /**
     * Inicializa las fuentes embebidas de manera thread-safe.
     * Intenta cargar Liberation Sans, con fallback a Helvetica built-in.
     */
    private synchronized void initFonts() {
        if (fontsInitialized) {
            return;
        }

        try {
            // Intentar cargar fuentes Liberation Sans embebidas
            BaseFont baseFont = loadEmbeddedFont("/fonts/LiberationSans-Regular.ttf");
            BaseFont baseFontBold = loadEmbeddedFont("/fonts/LiberationSans-Bold.ttf");

            titleFont = new Font(baseFontBold, 18, Font.NORMAL, TITLE_COLOR);
            subtitleFont = new Font(baseFontBold, 12, Font.NORMAL, SUBTITLE_COLOR);
            headerFont = new Font(baseFontBold, 10, Font.NORMAL, Color.WHITE);
            cellFont = new Font(baseFont, 9, Font.NORMAL, TITLE_COLOR);
            statsLabelFont = new Font(baseFontBold, 9, Font.NORMAL, SUBTITLE_COLOR);
            statsValueFont = new Font(baseFontBold, 11, Font.NORMAL, TITLE_COLOR);
            footerFont = new Font(baseFont, 8, Font.ITALIC, FOOTER_COLOR);

            LOG.info("Fuentes Liberation Sans cargadas correctamente para generación de PDF");
        } catch (Exception e) {
            LOG.warn("No se pudieron cargar fuentes embebidas, usando Helvetica como fallback: {}", e.getMessage());
            initFallbackFonts();
        }

        fontsInitialized = true;
    }

    private BaseFont loadEmbeddedFont(String resourcePath) throws DocumentException, IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Fuente no encontrada en classpath: " + resourcePath);
            }
            byte[] fontBytes = is.readAllBytes();
            return BaseFont.createFont(resourcePath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
        }
    }

    private void initFallbackFonts() {
        // Usar fuentes Helvetica built-in (funcionan en modo headless)
        titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, TITLE_COLOR);
        subtitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, SUBTITLE_COLOR);
        headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        cellFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TITLE_COLOR);
        statsLabelFont = new Font(Font.HELVETICA, 9, Font.BOLD, SUBTITLE_COLOR);
        statsValueFont = new Font(Font.HELVETICA, 11, Font.BOLD, TITLE_COLOR);
        footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, FOOTER_COLOR);
    }

    /**
     * Genera PDF para modo filtros con departamentos.
     */
    public byte[] generateDepartamentosReport(List<DivipolDepartamentoDTO> data, DivipolStatsDTO stats) {
        initFonts();
        return generateReport("Reporte de Departamentos", "Nacional", stats, document -> {
            addDepartamentosTable(document, data);
        });
    }

    /**
     * Genera PDF para modo filtros con municipios.
     */
    public byte[] generateMunicipiosReport(List<DivipolMunicipioDTO> data, DivipolStatsDTO stats, String nomDepto) {
        initFonts();
        String filterDesc = "Departamento: " + nomDepto;
        return generateReport("Reporte de Municipios", filterDesc, stats, document -> {
            addMunicipiosTable(document, data);
        });
    }

    /**
     * Genera PDF para modo filtros con zonas.
     */
    public byte[] generateZonasReport(List<DivipolZonaDTO> data, DivipolStatsDTO stats, String nomDepto, String nomMpio) {
        initFonts();
        String filterDesc = "Departamento: " + nomDepto + " | Municipio: " + nomMpio;
        return generateReport("Reporte de Zonas", filterDesc, stats, document -> {
            addZonasTable(document, data);
        });
    }

    /**
     * Genera PDF para modo filtros con puestos.
     */
    public byte[] generatePuestosReport(List<DivipolPuestoDTO> data, DivipolStatsDTO stats, String nomDepto, String nomMpio, int codZona) {
        initFonts();
        String filterDesc = "Departamento: " + nomDepto + " | Municipio: " + nomMpio + " | Zona: " + codZona;
        return generateReport("Reporte de Puestos", filterDesc, stats, document -> {
            addPuestosTable(document, data);
        });
    }

    /**
     * Genera PDF para modo búsqueda.
     */
    public byte[] generateSearchReport(List<DivipolSearchResultDTO> results, String searchTerm, String searchMode, long totalElements) {
        initFonts();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 54);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new FooterPageEvent(footerFont));

            document.open();

            // Header
            addHeader(document, "Resultados de Búsqueda");

            // Info de búsqueda
            Paragraph searchInfo = new Paragraph();
            searchInfo.add(new Chunk("Término: ", statsLabelFont));
            searchInfo.add(new Chunk("\"" + searchTerm + "\"", statsValueFont));
            searchInfo.add(new Chunk("  |  Modo: ", statsLabelFont));
            searchInfo.add(new Chunk("name".equals(searchMode) ? "Nombre" : "Código", statsValueFont));
            searchInfo.add(new Chunk("  |  Total: ", statsLabelFont));
            searchInfo.add(new Chunk(String.format("%,d registros", totalElements), statsValueFont));
            searchInfo.setSpacingAfter(15);
            document.add(searchInfo);

            // Tabla de resultados
            addSearchResultsTable(document, results);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de búsqueda", e);
        }
    }

    // =====================================================
    // Métodos privados
    // =====================================================

    @FunctionalInterface
    private interface TableAdder {
        void addTable(Document document) throws DocumentException;
    }

    private byte[] generateReport(String title, String filterDesc, DivipolStatsDTO stats, TableAdder tableAdder) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 54);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new FooterPageEvent(footerFont));

            document.open();

            // Header
            addHeader(document, title);

            // Filtros aplicados
            if (filterDesc != null && !filterDesc.isEmpty()) {
                Paragraph filters = new Paragraph("Filtros: " + filterDesc, subtitleFont);
                filters.setSpacingAfter(10);
                document.add(filters);
            }

            // Estadísticas
            if (stats != null) {
                addStatsSection(document, stats);
            }

            // Tabla de datos
            tableAdder.addTable(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private void addHeader(Document document, String title) throws DocumentException {
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(5);
        document.add(titlePara);

        Paragraph subtitle = new Paragraph("División Política Electoral de Colombia - DIVIPOL", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
    }

    private void addStatsSection(Document document, DivipolStatsDTO stats) throws DocumentException {
        PdfPTable statsTable = new PdfPTable(4);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(20);

        // Primera fila: conteos
        addStatsCell(statsTable, "Departamentos", formatNumber(stats.getTotalDepartamentos()));
        addStatsCell(statsTable, "Municipios", formatNumber(stats.getTotalMunicipios()));
        addStatsCell(statsTable, "Zonas", formatNumber(stats.getTotalZonas()));
        addStatsCell(statsTable, "Mesas", formatNumber(stats.getTotalMesas()));

        // Segunda fila: potencial electoral
        addStatsCell(statsTable, "Pot. Femenino", formatNumber(stats.getPotencialFemenino()));
        addStatsCell(statsTable, "Pot. Masculino", formatNumber(stats.getPotencialMasculino()));
        addStatsCell(statsTable, "Pot. Total", formatNumber(stats.getPotencialTotal()));
        addStatsCell(statsTable, "Puestos", formatNumber(stats.getTotalPuestos()));

        document.add(statsTable);
    }

    private void addStatsCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(STATS_BG);
        cell.setPadding(8);
        cell.setBorderColor(Color.WHITE);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", statsLabelFont));
        p.add(new Chunk(value, statsValueFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addDepartamentosTable(Document document, List<DivipolDepartamentoDTO> data) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 1f, 3f, 1.5f, 1.5f, 1.5f, 1f });
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        // Headers
        addHeaderCell(table, "Código");
        addHeaderCell(table, "Departamento");
        addHeaderCell(table, "Mujeres");
        addHeaderCell(table, "Hombres");
        addHeaderCell(table, "Total Potencial");
        addHeaderCell(table, "Mesas");

        // Data rows
        int row = 0;
        for (DivipolDepartamentoDTO dto : data) {
            Color bg = (row % 2 == 0) ? ROW_EVEN : ROW_ODD;
            addDataCell(table, String.format("%02d0000000", dto.getCoddepto()), bg);
            addDataCell(table, dto.getNomdepto(), bg);
            addDataCell(table, formatNumber(dto.getMujeres()), bg);
            addDataCell(table, formatNumber(dto.getHombres()), bg);
            addDataCell(table, formatNumber(dto.getTotalPotencial()), bg);
            addDataCell(table, formatNumber(dto.getMesas()), bg);
            row++;
        }

        document.add(table);
    }

    private void addMunicipiosTable(Document document, List<DivipolMunicipioDTO> data) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 1f, 3f, 1.5f, 1.5f, 1.5f, 1f });
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        // Headers
        addHeaderCell(table, "Cód. Divipol");
        addHeaderCell(table, "Municipio");
        addHeaderCell(table, "Pot. Femenino");
        addHeaderCell(table, "Pot. Masculino");
        addHeaderCell(table, "Total Potencial");
        addHeaderCell(table, "Mesas");

        // Data rows
        int row = 0;
        for (DivipolMunicipioDTO dto : data) {
            Color bg = (row % 2 == 0) ? ROW_EVEN : ROW_ODD;
            String codDivipol = String.format("%02d%03d0000", dto.getCoddepto(), dto.getCodmipio());
            addDataCell(table, codDivipol, bg);
            addDataCell(table, dto.getNommipio(), bg);
            addDataCell(table, formatNumber(dto.getPotencialFemenino()), bg);
            addDataCell(table, formatNumber(dto.getPotencialMasculino()), bg);
            addDataCell(table, formatNumber(dto.getPotencialTotal()), bg);
            addDataCell(table, formatNumber(dto.getMesas()), bg);
            row++;
        }

        document.add(table);
    }

    private void addZonasTable(Document document, List<DivipolZonaDTO> data) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 1f, 1.5f, 1.5f, 1.5f, 1f });
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        // Headers
        addHeaderCell(table, "Cód. Divipol");
        addHeaderCell(table, "Zona");
        addHeaderCell(table, "Pot. Femenino");
        addHeaderCell(table, "Pot. Masculino");
        addHeaderCell(table, "Total Potencial");
        addHeaderCell(table, "Mesas");

        // Data rows
        int row = 0;
        for (DivipolZonaDTO dto : data) {
            Color bg = (row % 2 == 0) ? ROW_EVEN : ROW_ODD;
            String codDivipol = String.format("%02d%03d%02d00", dto.getCoddepto(), dto.getCodmipio(), dto.getCodzona());
            addDataCell(table, codDivipol, bg);
            addDataCell(table, "Zona " + dto.getCodzona(), bg);
            addDataCell(table, formatNumber(dto.getPotencialFemenino()), bg);
            addDataCell(table, formatNumber(dto.getPotencialMasculino()), bg);
            addDataCell(table, formatNumber(dto.getPotencialTotal()), bg);
            addDataCell(table, formatNumber(dto.getMesas()), bg);
            row++;
        }

        document.add(table);
    }

    private void addPuestosTable(Document document, List<DivipolPuestoDTO> data) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 3f, 1.5f, 1.5f, 1.5f, 1f });
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        // Headers
        addHeaderCell(table, "Cód. Divipol");
        addHeaderCell(table, "Puesto");
        addHeaderCell(table, "Pot. Femenino");
        addHeaderCell(table, "Pot. Masculino");
        addHeaderCell(table, "Total Potencial");
        addHeaderCell(table, "Mesas");

        // Data rows
        int row = 0;
        for (DivipolPuestoDTO dto : data) {
            Color bg = (row % 2 == 0) ? ROW_EVEN : ROW_ODD;
            String codDivipol = String.format("%02d%03d%02d%s", dto.getCoddepto(), dto.getCodmipio(), dto.getCodzona(), dto.getCodpuesto());
            addDataCell(table, codDivipol, bg);
            addDataCell(table, dto.getNompuesto(), bg);
            addDataCell(table, formatNumber(dto.getPotencialFemenino()), bg);
            addDataCell(table, formatNumber(dto.getPotencialMasculino()), bg);
            addDataCell(table, formatNumber(dto.getPotencialTotal()), bg);
            addDataCell(table, formatNumber(dto.getMesas()), bg);
            row++;
        }

        document.add(table);
    }

    private void addSearchResultsTable(Document document, List<DivipolSearchResultDTO> results) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 0.8f, 2.5f, 2f, 1.2f, 0.8f });
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        // Headers
        addHeaderCell(table, "Código");
        addHeaderCell(table, "Tipo");
        addHeaderCell(table, "Nombre");
        addHeaderCell(table, "Ubicación");
        addHeaderCell(table, "Potencial");
        addHeaderCell(table, "Mesas");

        // Data rows
        int row = 0;
        for (DivipolSearchResultDTO dto : results) {
            Color bg = (row % 2 == 0) ? ROW_EVEN : ROW_ODD;
            addDataCell(table, dto.getCodigoDivipol(), bg);
            addDataCell(table, dto.getTipo().toString(), bg);
            addDataCell(table, getDisplayName(dto), bg);
            addDataCell(table, getLocation(dto), bg);
            addDataCell(table, formatNumber(dto.getPotencialTotal()), bg);
            addDataCell(table, formatNumber(dto.getMesas()), bg);
            row++;
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", cellFont));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setBorderColor(BORDER_COLOR);
        table.addCell(cell);
    }

    private String formatNumber(Long value) {
        if (value == null) return "0";
        return String.format("%,d", value);
    }

    private String getDisplayName(DivipolSearchResultDTO dto) {
        if (dto.getTipo() == null) return "";
        switch (dto.getTipo()) {
            case DEPTO:
                return dto.getNomdepto();
            case MPIO:
                return dto.getNommipio();
            case PUESTO:
                return dto.getNompuesto();
            case ZONA:
                return "Zona " + dto.getCodzona();
            default:
                return "";
        }
    }

    private String getLocation(DivipolSearchResultDTO dto) {
        if (dto.getTipo() == null) return "";
        switch (dto.getTipo()) {
            case DEPTO:
                return "Colombia";
            case MPIO:
                return dto.getNomdepto();
            case ZONA:
            case PUESTO:
                return dto.getNomdepto() + " > " + dto.getNommipio();
            default:
                return "";
        }
    }

    /**
     * Page event handler para agregar footer con fecha y número de página.
     * Recibe la fuente como parámetro para evitar problemas con campos estáticos.
     */
    private static class FooterPageEvent extends PdfPageEventHelper {

        private final Font footerFont;

        FooterPageEvent(Font footerFont) {
            this.footerFont = footerFont;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String footer = "Generado: " + dateStr + "  |  Página " + writer.getPageNumber();

            ColumnText.showTextAligned(
                cb,
                Element.ALIGN_CENTER,
                new Phrase(footer, footerFont),
                (document.right() - document.left()) / 2 + document.leftMargin(),
                document.bottom() - 20,
                0
            );
        }
    }
}

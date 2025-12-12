package com.tyse.scrutiny.micro.divipol.service.export;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Generador de reportes PDF usando OpenPDF.
 */
@Component
public class DivipolPdfGenerator {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(51, 51, 51));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(102, 102, 102));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(51, 51, 51));
    private static final Font STATS_LABEL_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(102, 102, 102));
    private static final Font STATS_VALUE_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(51, 51, 51));
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(128, 128, 128));

    private static final Color HEADER_BG = new Color(52, 73, 94);
    private static final Color ROW_EVEN = new Color(245, 245, 245);
    private static final Color ROW_ODD = Color.WHITE;
    private static final Color STATS_BG = new Color(236, 240, 241);

    /**
     * Genera PDF para modo filtros con departamentos.
     */
    public byte[] generateDepartamentosReport(List<DivipolDepartamentoDTO> data, DivipolStatsDTO stats) {
        return generateReport("Reporte de Departamentos", "Nacional", stats, document -> {
            addDepartamentosTable(document, data);
        });
    }

    /**
     * Genera PDF para modo filtros con municipios.
     */
    public byte[] generateMunicipiosReport(List<DivipolMunicipioDTO> data, DivipolStatsDTO stats, String nomDepto) {
        String filterDesc = "Departamento: " + nomDepto;
        return generateReport("Reporte de Municipios", filterDesc, stats, document -> {
            addMunicipiosTable(document, data);
        });
    }

    /**
     * Genera PDF para modo filtros con zonas.
     */
    public byte[] generateZonasReport(List<DivipolZonaDTO> data, DivipolStatsDTO stats, String nomDepto, String nomMpio) {
        String filterDesc = "Departamento: " + nomDepto + " | Municipio: " + nomMpio;
        return generateReport("Reporte de Zonas", filterDesc, stats, document -> {
            addZonasTable(document, data);
        });
    }

    /**
     * Genera PDF para modo filtros con puestos.
     */
    public byte[] generatePuestosReport(List<DivipolPuestoDTO> data, DivipolStatsDTO stats, String nomDepto, String nomMpio, int codZona) {
        String filterDesc = "Departamento: " + nomDepto + " | Municipio: " + nomMpio + " | Zona: " + codZona;
        return generateReport("Reporte de Puestos", filterDesc, stats, document -> {
            addPuestosTable(document, data);
        });
    }

    /**
     * Genera PDF para modo búsqueda.
     */
    public byte[] generateSearchReport(List<DivipolSearchResultDTO> results, String searchTerm, String searchMode, long totalElements) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 54);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new FooterPageEvent());

            document.open();

            // Header
            addHeader(document, "Resultados de Búsqueda");

            // Info de búsqueda
            Paragraph searchInfo = new Paragraph();
            searchInfo.add(new Chunk("Término: ", STATS_LABEL_FONT));
            searchInfo.add(new Chunk("\"" + searchTerm + "\"", STATS_VALUE_FONT));
            searchInfo.add(new Chunk("  |  Modo: ", STATS_LABEL_FONT));
            searchInfo.add(new Chunk("name".equals(searchMode) ? "Nombre" : "Código", STATS_VALUE_FONT));
            searchInfo.add(new Chunk("  |  Total: ", STATS_LABEL_FONT));
            searchInfo.add(new Chunk(String.format("%,d registros", totalElements), STATS_VALUE_FONT));
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
            writer.setPageEvent(new FooterPageEvent());

            document.open();

            // Header
            addHeader(document, title);

            // Filtros aplicados
            if (filterDesc != null && !filterDesc.isEmpty()) {
                Paragraph filters = new Paragraph("Filtros: " + filterDesc, SUBTITLE_FONT);
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
        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(5);
        document.add(titlePara);

        Paragraph subtitle = new Paragraph("División Política Electoral de Colombia - DIVIPOL", SUBTITLE_FONT);
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
        p.add(new Chunk(label + "\n", STATS_LABEL_FONT));
        p.add(new Chunk(value, STATS_VALUE_FONT));
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
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", CELL_FONT));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setBorderColor(new Color(220, 220, 220));
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
     */
    private static class FooterPageEvent extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String footer = "Generado: " + dateStr + "  |  Página " + writer.getPageNumber();

            ColumnText.showTextAligned(
                cb,
                Element.ALIGN_CENTER,
                new Phrase(footer, FOOTER_FONT),
                (document.right() - document.left()) / 2 + document.leftMargin(),
                document.bottom() - 20,
                0
            );
        }
    }
}

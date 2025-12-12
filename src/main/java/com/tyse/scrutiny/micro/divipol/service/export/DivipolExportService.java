package com.tyse.scrutiny.micro.divipol.service.export;

import com.tyse.scrutiny.micro.divipol.repository.DivipolRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio para exportar datos de DIVIPOL a CSV y PDF.
 */
@Service
public class DivipolExportService {

    private static final Logger LOG = LoggerFactory.getLogger(DivipolExportService.class);

    private static final int MAX_EXPORT_RECORDS = 10000;
    private static final String MODE_NAME = "name";
    private static final String MODE_CODE = "code";

    private final DivipolRepository divipolRepository;
    private final DivipolPdfGenerator pdfGenerator;

    public DivipolExportService(DivipolRepository divipolRepository, DivipolPdfGenerator pdfGenerator) {
        this.divipolRepository = divipolRepository;
        this.pdfGenerator = pdfGenerator;
    }

    // =====================================================
    // Exportación Modo Filtros - CSV
    // =====================================================

    /**
     * Exporta datos del modo filtros a CSV.
     * El nivel de datos depende de qué filtros estén aplicados.
     */
    public Mono<byte[]> exportFiltersToCsv(Integer codDepto, Integer codMpio, Integer codZona) {
        LOG.debug("Exportando filtros a CSV: depto={}, mpio={}, zona={}", codDepto, codMpio, codZona);

        if (codZona != null && codMpio != null && codDepto != null) {
            // Nivel: Puestos
            return exportPuestosToCsv(codDepto, codMpio, codZona);
        } else if (codMpio != null && codDepto != null) {
            // Nivel: Zonas
            return exportZonasToCsv(codDepto, codMpio);
        } else if (codDepto != null) {
            // Nivel: Municipios
            return exportMunicipiosToCsv(codDepto);
        } else {
            // Nivel: Departamentos
            return exportDepartamentosToCsv();
        }
    }

    private Mono<byte[]> exportDepartamentosToCsv() {
        return divipolRepository
            .findAllDepartamentos()
            .collectList()
            .zipWith(divipolRepository.getGeneralStats())
            .map(tuple -> {
                List<DivipolDepartamentoDTO> data = tuple.getT1();
                DivipolStatsDTO stats = tuple.getT2();
                return generateDepartamentosCsv(data, stats);
            });
    }

    private Mono<byte[]> exportMunicipiosToCsv(Integer codDepto) {
        return divipolRepository
            .findMunicipiosByDepartamento(codDepto)
            .collectList()
            .zipWith(divipolRepository.getStatsByDepartamento(codDepto))
            .map(tuple -> {
                List<DivipolMunicipioDTO> data = tuple.getT1();
                DivipolStatsDTO stats = tuple.getT2();
                String nomDepto = data.isEmpty() ? "" : data.get(0).getNomdepto();
                return generateMunicipiosCsv(data, stats, nomDepto);
            });
    }

    private Mono<byte[]> exportZonasToCsv(Integer codDepto, Integer codMpio) {
        return divipolRepository
            .findZonasByMunicipio(codDepto, codMpio)
            .collectList()
            .zipWith(divipolRepository.getStatsByMunicipio(codDepto, codMpio))
            .map(tuple -> {
                List<DivipolZonaDTO> data = tuple.getT1();
                DivipolStatsDTO stats = tuple.getT2();
                String nomDepto = data.isEmpty() ? "" : data.get(0).getNomdepto();
                String nomMpio = data.isEmpty() ? "" : data.get(0).getNommipio();
                return generateZonasCsv(data, stats, nomDepto, nomMpio);
            });
    }

    private Mono<byte[]> exportPuestosToCsv(Integer codDepto, Integer codMpio, Integer codZona) {
        return divipolRepository
            .findPuestosByZona(codDepto, codMpio, codZona)
            .collectList()
            .zipWith(divipolRepository.getStatsByZona(codDepto, codMpio, codZona))
            .map(tuple -> {
                List<DivipolPuestoDTO> data = tuple.getT1();
                DivipolStatsDTO stats = tuple.getT2();
                String nomDepto = data.isEmpty() ? "" : data.get(0).getNomdepto();
                String nomMpio = data.isEmpty() ? "" : data.get(0).getNommipio();
                return generatePuestosCsv(data, stats, nomDepto, nomMpio, codZona);
            });
    }

    // =====================================================
    // Exportación Modo Filtros - PDF
    // =====================================================

    /**
     * Exporta datos del modo filtros a PDF.
     */
    public Mono<byte[]> exportFiltersToPdf(Integer codDepto, Integer codMpio, Integer codZona) {
        LOG.debug("Exportando filtros a PDF: depto={}, mpio={}, zona={}", codDepto, codMpio, codZona);

        if (codZona != null && codMpio != null && codDepto != null) {
            return exportPuestosToPdf(codDepto, codMpio, codZona);
        } else if (codMpio != null && codDepto != null) {
            return exportZonasToPdf(codDepto, codMpio);
        } else if (codDepto != null) {
            return exportMunicipiosToPdf(codDepto);
        } else {
            return exportDepartamentosToPdf();
        }
    }

    private Mono<byte[]> exportDepartamentosToPdf() {
        return divipolRepository
            .findAllDepartamentos()
            .collectList()
            .zipWith(divipolRepository.getGeneralStats())
            .map(tuple -> pdfGenerator.generateDepartamentosReport(tuple.getT1(), tuple.getT2()));
    }

    private Mono<byte[]> exportMunicipiosToPdf(Integer codDepto) {
        return divipolRepository
            .findMunicipiosByDepartamento(codDepto)
            .collectList()
            .zipWith(divipolRepository.getStatsByDepartamento(codDepto))
            .map(tuple -> {
                List<DivipolMunicipioDTO> data = tuple.getT1();
                String nomDepto = data.isEmpty() ? "" : data.get(0).getNomdepto();
                return pdfGenerator.generateMunicipiosReport(data, tuple.getT2(), nomDepto);
            });
    }

    private Mono<byte[]> exportZonasToPdf(Integer codDepto, Integer codMpio) {
        return divipolRepository
            .findZonasByMunicipio(codDepto, codMpio)
            .collectList()
            .zipWith(divipolRepository.getStatsByMunicipio(codDepto, codMpio))
            .map(tuple -> {
                List<DivipolZonaDTO> data = tuple.getT1();
                String nomDepto = data.isEmpty() ? "" : data.get(0).getNomdepto();
                String nomMpio = data.isEmpty() ? "" : data.get(0).getNommipio();
                return pdfGenerator.generateZonasReport(data, tuple.getT2(), nomDepto, nomMpio);
            });
    }

    private Mono<byte[]> exportPuestosToPdf(Integer codDepto, Integer codMpio, Integer codZona) {
        return divipolRepository
            .findPuestosByZona(codDepto, codMpio, codZona)
            .collectList()
            .zipWith(divipolRepository.getStatsByZona(codDepto, codMpio, codZona))
            .map(tuple -> {
                List<DivipolPuestoDTO> data = tuple.getT1();
                String nomDepto = data.isEmpty() ? "" : data.get(0).getNomdepto();
                String nomMpio = data.isEmpty() ? "" : data.get(0).getNommipio();
                return pdfGenerator.generatePuestosReport(data, tuple.getT2(), nomDepto, nomMpio, codZona);
            });
    }

    // =====================================================
    // Exportación Modo Búsqueda - CSV
    // =====================================================

    /**
     * Exporta resultados de búsqueda a CSV.
     */
    public Mono<byte[]> exportSearchToCsv(String q, String mode, Integer page, Integer size, Boolean exportAll) {
        LOG.debug("Exportando búsqueda a CSV: q={}, mode={}, exportAll={}", q, mode, exportAll);

        String searchMode = mode != null ? mode : MODE_NAME;
        boolean exportAllResults = Boolean.TRUE.equals(exportAll);

        if (exportAllResults) {
            // Exportar todos los resultados (máximo MAX_EXPORT_RECORDS)
            return getSearchResultsForExport(q, searchMode, 0, MAX_EXPORT_RECORDS)
                .zipWith(getSearchCount(q, searchMode))
                .map(tuple -> generateSearchCsv(tuple.getT1(), q, searchMode, tuple.getT2()));
        } else {
            // Exportar solo la página actual
            int pageNum = page != null ? page : 0;
            int pageSize = size != null ? size : 20;
            return getSearchResultsForExport(q, searchMode, pageNum, pageSize)
                .zipWith(getSearchCount(q, searchMode))
                .map(tuple -> generateSearchCsv(tuple.getT1(), q, searchMode, tuple.getT2()));
        }
    }

    // =====================================================
    // Exportación Modo Búsqueda - PDF
    // =====================================================

    /**
     * Exporta resultados de búsqueda a PDF.
     */
    public Mono<byte[]> exportSearchToPdf(String q, String mode, Integer page, Integer size, Boolean exportAll) {
        LOG.debug("Exportando búsqueda a PDF: q={}, mode={}, exportAll={}", q, mode, exportAll);

        String searchMode = mode != null ? mode : MODE_NAME;
        boolean exportAllResults = Boolean.TRUE.equals(exportAll);

        if (exportAllResults) {
            return getSearchResultsForExport(q, searchMode, 0, MAX_EXPORT_RECORDS)
                .zipWith(getSearchCount(q, searchMode))
                .map(tuple -> pdfGenerator.generateSearchReport(tuple.getT1(), q, searchMode, tuple.getT2()));
        } else {
            int pageNum = page != null ? page : 0;
            int pageSize = size != null ? size : 20;
            return getSearchResultsForExport(q, searchMode, pageNum, pageSize)
                .zipWith(getSearchCount(q, searchMode))
                .map(tuple -> pdfGenerator.generateSearchReport(tuple.getT1(), q, searchMode, tuple.getT2()));
        }
    }

    private Mono<List<DivipolSearchResultDTO>> getSearchResultsForExport(String q, String mode, int page, int size) {
        if (MODE_CODE.equals(mode)) {
            return divipolRepository.searchByCode(q.trim(), page, size).collectList();
        } else {
            return divipolRepository.searchByName(q.trim(), page, size).collectList();
        }
    }

    private Mono<Long> getSearchCount(String q, String mode) {
        if (MODE_CODE.equals(mode)) {
            return divipolRepository.countSearchByCode(q.trim());
        } else {
            return divipolRepository.countSearchByName(q.trim());
        }
    }

    // =====================================================
    // Generadores de CSV
    // =====================================================

    private byte[] generateDepartamentosCsv(List<DivipolDepartamentoDTO> data, DivipolStatsDTO stats) {
        StringBuilder csv = new StringBuilder();
        csv.append(generateCsvHeader(stats, "Nacional"));
        csv.append("Código,Departamento,Mujeres,Hombres,Total Potencial,Mesas\n");
        for (DivipolDepartamentoDTO dto : data) {
            csv.append(
                String.format(
                    "%02d0000000,%s,%d,%d,%d,%d\n",
                    dto.getCoddepto(),
                    escapeCsv(dto.getNomdepto()),
                    dto.getMujeres(),
                    dto.getHombres(),
                    dto.getTotalPotencial(),
                    dto.getMesas()
                )
            );
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateMunicipiosCsv(List<DivipolMunicipioDTO> data, DivipolStatsDTO stats, String nomDepto) {
        StringBuilder csv = new StringBuilder();
        csv.append(generateCsvHeader(stats, "Departamento: " + nomDepto));
        csv.append("Cód. Divipol,Municipio,Pot. Femenino,Pot. Masculino,Total Potencial,Mesas\n");
        for (DivipolMunicipioDTO dto : data) {
            String codDivipol = String.format("%02d%03d0000", dto.getCoddepto(), dto.getCodmipio());
            csv.append(
                String.format(
                    "%s,%s,%d,%d,%d,%d\n",
                    codDivipol,
                    escapeCsv(dto.getNommipio()),
                    dto.getPotencialFemenino(),
                    dto.getPotencialMasculino(),
                    dto.getPotencialTotal(),
                    dto.getMesas()
                )
            );
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateZonasCsv(List<DivipolZonaDTO> data, DivipolStatsDTO stats, String nomDepto, String nomMpio) {
        StringBuilder csv = new StringBuilder();
        csv.append(generateCsvHeader(stats, "Departamento: " + nomDepto + " | Municipio: " + nomMpio));
        csv.append("Cód. Divipol,Zona,Pot. Femenino,Pot. Masculino,Total Potencial,Mesas\n");
        for (DivipolZonaDTO dto : data) {
            String codDivipol = String.format("%02d%03d%02d00", dto.getCoddepto(), dto.getCodmipio(), dto.getCodzona());
            csv.append(
                String.format(
                    "%s,Zona %d,%d,%d,%d,%d\n",
                    codDivipol,
                    dto.getCodzona(),
                    dto.getPotencialFemenino(),
                    dto.getPotencialMasculino(),
                    dto.getPotencialTotal(),
                    dto.getMesas()
                )
            );
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generatePuestosCsv(
        List<DivipolPuestoDTO> data,
        DivipolStatsDTO stats,
        String nomDepto,
        String nomMpio,
        Integer codZona
    ) {
        StringBuilder csv = new StringBuilder();
        csv.append(generateCsvHeader(stats, "Departamento: " + nomDepto + " | Municipio: " + nomMpio + " | Zona: " + codZona));
        csv.append("Cód. Divipol,Puesto,Pot. Femenino,Pot. Masculino,Total Potencial,Mesas\n");
        for (DivipolPuestoDTO dto : data) {
            String codDivipol = String.format("%02d%03d%02d%s", dto.getCoddepto(), dto.getCodmipio(), dto.getCodzona(), dto.getCodpuesto());
            csv.append(
                String.format(
                    "%s,%s,%d,%d,%d,%d\n",
                    codDivipol,
                    escapeCsv(dto.getNompuesto()),
                    dto.getPotencialFemenino(),
                    dto.getPotencialMasculino(),
                    dto.getPotencialTotal(),
                    dto.getMesas()
                )
            );
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateSearchCsv(List<DivipolSearchResultDTO> data, String searchTerm, String mode, long total) {
        StringBuilder csv = new StringBuilder();

        // Header con info de búsqueda
        csv.append("# Reporte de Búsqueda DIVIPOL\n");
        csv.append("# Término: ").append(searchTerm).append("\n");
        csv.append("# Modo: ").append(MODE_NAME.equals(mode) ? "Nombre" : "Código").append("\n");
        csv.append("# Total resultados: ").append(total).append("\n");
        csv.append("# Generado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        // Datos
        csv.append("Código,Tipo,Nombre,Ubicación,Potencial Total,Mesas\n");
        for (DivipolSearchResultDTO dto : data) {
            csv.append(
                String.format(
                    "%s,%s,%s,%s,%d,%d\n",
                    dto.getCodigoDivipol(),
                    dto.getTipo(),
                    escapeCsv(getDisplayName(dto)),
                    escapeCsv(getLocation(dto)),
                    dto.getPotencialTotal(),
                    dto.getMesas()
                )
            );
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String generateCsvHeader(DivipolStatsDTO stats, String filterDesc) {
        StringBuilder header = new StringBuilder();
        header.append("# Reporte DIVIPOL - División Política Electoral de Colombia\n");
        header.append("# Filtros: ").append(filterDesc).append("\n");
        header.append("# Generado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        header.append("#\n");
        header.append("# ESTADÍSTICAS\n");
        header.append("# Departamentos: ").append(stats.getTotalDepartamentos()).append("\n");
        header.append("# Municipios: ").append(stats.getTotalMunicipios()).append("\n");
        header.append("# Zonas: ").append(stats.getTotalZonas()).append("\n");
        header.append("# Puestos: ").append(stats.getTotalPuestos()).append("\n");
        header.append("# Mesas: ").append(stats.getTotalMesas()).append("\n");
        header.append("# Potencial Femenino: ").append(stats.getPotencialFemenino()).append("\n");
        header.append("# Potencial Masculino: ").append(stats.getPotencialMasculino()).append("\n");
        header.append("# Potencial Total: ").append(stats.getPotencialTotal()).append("\n");
        header.append("#\n");
        header.append("# DATOS\n");
        return header.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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
}

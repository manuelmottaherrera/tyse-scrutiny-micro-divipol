package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación del repositorio personalizado para Divipol
 * Usa las vistas agregadas (view_divipol_*) para consultas optimizadas
 */
@Repository
public class DivipolRepositoryCustomImpl implements DivipolRepositoryCustom {

    private final DatabaseClient databaseClient;

    public DivipolRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<DivipolDepartamentoDTO> findAllDepartamentos() {
        String sql = "SELECT coddepto, nomdepto, mujeres, hombres, total_potencial, mesas " + "FROM view_divipol_departamento";

        return databaseClient
            .sql(sql)
            .map(row ->
                new DivipolDepartamentoDTO()
                    .coddepto(row.get("coddepto", Integer.class))
                    .nomdepto(row.get("nomdepto", String.class))
                    .mujeres(row.get("mujeres", Long.class))
                    .hombres(row.get("hombres", Long.class))
                    .totalPotencial(row.get("total_potencial", Long.class))
                    .mesas(row.get("mesas", Long.class))
            )
            .all();
    }

    @Override
    public Flux<DivipolMunicipioDTO> findMunicipiosByDepartamento(Integer codDepto) {
        String sql =
            "SELECT coddepto, codmipio, nomdepto, nommipio, " +
            "potencial_femenino, potencial_masculino, potencial_total, mesas " +
            "FROM view_divipol_municipio " +
            "WHERE coddepto = :coddepto";

        return databaseClient
            .sql(sql)
            .bind("coddepto", codDepto)
            .map(row ->
                new DivipolMunicipioDTO()
                    .coddepto(row.get("coddepto", Integer.class))
                    .codmipio(row.get("codmipio", Integer.class))
                    .nomdepto(row.get("nomdepto", String.class))
                    .nommipio(row.get("nommipio", String.class))
                    .potencialFemenino(row.get("potencial_femenino", Long.class))
                    .potencialMasculino(row.get("potencial_masculino", Long.class))
                    .potencialTotal(row.get("potencial_total", Long.class))
                    .mesas(row.get("mesas", Long.class))
            )
            .all();
    }

    @Override
    public Flux<DivipolZonaDTO> findZonasByMunicipio(Integer codDepto, Integer codMpio) {
        String sql =
            "SELECT coddepto, codmipio, codzona, nomdepto, nommipio, " +
            "potencial_femenino, potencial_masculino, potencial_total, mesas " +
            "FROM view_divipol_zona " +
            "WHERE coddepto = :coddepto AND codmipio = :codmipio";

        return databaseClient
            .sql(sql)
            .bind("coddepto", codDepto)
            .bind("codmipio", codMpio)
            .map(row ->
                new DivipolZonaDTO()
                    .coddepto(row.get("coddepto", Integer.class))
                    .codmipio(row.get("codmipio", Integer.class))
                    .codzona(row.get("codzona", Integer.class))
                    .nomdepto(row.get("nomdepto", String.class))
                    .nommipio(row.get("nommipio", String.class))
                    .potencialFemenino(row.get("potencial_femenino", Long.class))
                    .potencialMasculino(row.get("potencial_masculino", Long.class))
                    .potencialTotal(row.get("potencial_total", Long.class))
                    .mesas(row.get("mesas", Long.class))
            )
            .all();
    }

    @Override
    public Flux<DivipolPuestoDTO> findPuestosByZona(Integer codDepto, Integer codMpio, Integer codZona) {
        String sql =
            "SELECT coddepto, codmipio, codzona, codpuesto, nomdepto, nommipio, nompuesto, " +
            "potencial_femenino, potencial_masculino, potencial_total, mesas " +
            "FROM view_divipol_puesto " +
            "WHERE coddepto = :coddepto AND codmipio = :codmipio AND codzona = :codzona";

        return databaseClient
            .sql(sql)
            .bind("coddepto", codDepto)
            .bind("codmipio", codMpio)
            .bind("codzona", codZona)
            .map(row ->
                new DivipolPuestoDTO()
                    .coddepto(row.get("coddepto", Integer.class))
                    .codmipio(row.get("codmipio", Integer.class))
                    .codzona(row.get("codzona", Integer.class))
                    .codpuesto(row.get("codpuesto", String.class))
                    .nomdepto(row.get("nomdepto", String.class))
                    .nommipio(row.get("nommipio", String.class))
                    .nompuesto(row.get("nompuesto", String.class))
                    .potencialFemenino(row.get("potencial_femenino", Long.class))
                    .potencialMasculino(row.get("potencial_masculino", Long.class))
                    .potencialTotal(row.get("potencial_total", Long.class))
                    .mesas(row.get("mesas", Long.class))
            )
            .all();
    }

    @Override
    public Mono<DivipolStatsDTO> getGeneralStats() {
        // Usa las vistas para contar totales y sumar potenciales
        String sql =
            "SELECT " +
            "(SELECT COUNT(*) FROM view_divipol_departamento) AS total_departamentos, " +
            "(SELECT COUNT(*) FROM view_divipol_municipio) AS total_municipios, " +
            "(SELECT COUNT(*) FROM view_divipol_zona) AS total_zonas, " +
            "(SELECT COUNT(*) FROM view_divipol_puesto) AS total_puestos, " +
            "(SELECT SUM(mesas) FROM view_divipol_departamento) AS total_mesas, " +
            "(SELECT SUM(mujeres) FROM view_divipol_departamento) AS potencial_femenino, " +
            "(SELECT SUM(hombres) FROM view_divipol_departamento) AS potencial_masculino, " +
            "(SELECT SUM(total_potencial) FROM view_divipol_departamento) AS potencial_total";

        return databaseClient
            .sql(sql)
            .map(row ->
                new DivipolStatsDTO()
                    .totalDepartamentos(row.get("total_departamentos", Long.class))
                    .totalMunicipios(row.get("total_municipios", Long.class))
                    .totalZonas(row.get("total_zonas", Long.class))
                    .totalPuestos(row.get("total_puestos", Long.class))
                    .totalMesas(row.get("total_mesas", Long.class))
                    .potencialFemenino(row.get("potencial_femenino", Long.class))
                    .potencialMasculino(row.get("potencial_masculino", Long.class))
                    .potencialTotal(row.get("potencial_total", Long.class))
            )
            .one();
    }

    @Override
    public Mono<DivipolStatsDTO> getStatsByDepartamento(Integer codDepto) {
        // Usa view_divipol_municipio para contar municipios del departamento
        String sql =
            "SELECT " +
            "1 AS total_departamentos, " +
            "(SELECT COUNT(*) FROM view_divipol_municipio WHERE coddepto = :coddepto) AS total_municipios, " +
            "(SELECT COUNT(*) FROM view_divipol_zona WHERE coddepto = :coddepto) AS total_zonas, " +
            "(SELECT COUNT(*) FROM view_divipol_puesto WHERE coddepto = :coddepto) AS total_puestos, " +
            "(SELECT SUM(mesas) FROM view_divipol_municipio WHERE coddepto = :coddepto) AS total_mesas, " +
            "(SELECT SUM(potencial_femenino) FROM view_divipol_municipio WHERE coddepto = :coddepto) AS potencial_femenino, " +
            "(SELECT SUM(potencial_masculino) FROM view_divipol_municipio WHERE coddepto = :coddepto) AS potencial_masculino, " +
            "(SELECT SUM(potencial_total) FROM view_divipol_municipio WHERE coddepto = :coddepto) AS potencial_total";

        return databaseClient
            .sql(sql)
            .bind("coddepto", codDepto)
            .map(row ->
                new DivipolStatsDTO()
                    .totalDepartamentos(row.get("total_departamentos", Long.class))
                    .totalMunicipios(row.get("total_municipios", Long.class))
                    .totalZonas(row.get("total_zonas", Long.class))
                    .totalPuestos(row.get("total_puestos", Long.class))
                    .totalMesas(row.get("total_mesas", Long.class))
                    .potencialFemenino(row.get("potencial_femenino", Long.class))
                    .potencialMasculino(row.get("potencial_masculino", Long.class))
                    .potencialTotal(row.get("potencial_total", Long.class))
            )
            .one();
    }

    @Override
    public Mono<DivipolStatsDTO> getStatsByMunicipio(Integer codDepto, Integer codMpio) {
        // Usa view_divipol_zona para contar zonas del municipio
        String sql =
            "SELECT " +
            "1 AS total_departamentos, " +
            "1 AS total_municipios, " +
            "(SELECT COUNT(*) FROM view_divipol_zona WHERE coddepto = :coddepto AND codmipio = :codmipio) AS total_zonas, " +
            "(SELECT COUNT(*) FROM view_divipol_puesto WHERE coddepto = :coddepto AND codmipio = :codmipio) AS total_puestos, " +
            "(SELECT SUM(mesas) FROM view_divipol_zona WHERE coddepto = :coddepto AND codmipio = :codmipio) AS total_mesas, " +
            "(SELECT SUM(potencial_femenino) FROM view_divipol_zona WHERE coddepto = :coddepto AND codmipio = :codmipio) AS potencial_femenino, " +
            "(SELECT SUM(potencial_masculino) FROM view_divipol_zona WHERE coddepto = :coddepto AND codmipio = :codmipio) AS potencial_masculino, " +
            "(SELECT SUM(potencial_total) FROM view_divipol_zona WHERE coddepto = :coddepto AND codmipio = :codmipio) AS potencial_total";

        return databaseClient
            .sql(sql)
            .bind("coddepto", codDepto)
            .bind("codmipio", codMpio)
            .map(row ->
                new DivipolStatsDTO()
                    .totalDepartamentos(row.get("total_departamentos", Long.class))
                    .totalMunicipios(row.get("total_municipios", Long.class))
                    .totalZonas(row.get("total_zonas", Long.class))
                    .totalPuestos(row.get("total_puestos", Long.class))
                    .totalMesas(row.get("total_mesas", Long.class))
                    .potencialFemenino(row.get("potencial_femenino", Long.class))
                    .potencialMasculino(row.get("potencial_masculino", Long.class))
                    .potencialTotal(row.get("potencial_total", Long.class))
            )
            .one();
    }

    @Override
    public Mono<DivipolStatsDTO> getStatsByZona(Integer codDepto, Integer codMpio, Integer codZona) {
        // Usa view_divipol_puesto para contar puestos de la zona
        String sql =
            "SELECT " +
            "1 AS total_departamentos, " +
            "1 AS total_municipios, " +
            "1 AS total_zonas, " +
            "(SELECT COUNT(*) FROM view_divipol_puesto WHERE coddepto = :coddepto AND codmipio = :codmipio AND codzona = :codzona) AS total_puestos, " +
            "(SELECT SUM(mesas) FROM view_divipol_puesto WHERE coddepto = :coddepto AND codmipio = :codmipio AND codzona = :codzona) AS total_mesas, " +
            "(SELECT SUM(potencial_femenino) FROM view_divipol_puesto WHERE coddepto = :coddepto AND codmipio = :codmipio AND codzona = :codzona) AS potencial_femenino, " +
            "(SELECT SUM(potencial_masculino) FROM view_divipol_puesto WHERE coddepto = :coddepto AND codmipio = :codmipio AND codzona = :codzona) AS potencial_masculino, " +
            "(SELECT SUM(potencial_total) FROM view_divipol_puesto WHERE coddepto = :coddepto AND codmipio = :codmipio AND codzona = :codzona) AS potencial_total";

        return databaseClient
            .sql(sql)
            .bind("coddepto", codDepto)
            .bind("codmipio", codMpio)
            .bind("codzona", codZona)
            .map(row ->
                new DivipolStatsDTO()
                    .totalDepartamentos(row.get("total_departamentos", Long.class))
                    .totalMunicipios(row.get("total_municipios", Long.class))
                    .totalZonas(row.get("total_zonas", Long.class))
                    .totalPuestos(row.get("total_puestos", Long.class))
                    .totalMesas(row.get("total_mesas", Long.class))
                    .potencialFemenino(row.get("potencial_femenino", Long.class))
                    .potencialMasculino(row.get("potencial_masculino", Long.class))
                    .potencialTotal(row.get("potencial_total", Long.class))
            )
            .one();
    }

    // =====================================================
    // Métodos de Búsqueda Full-Text y por Código
    // =====================================================

    /**
     * SQL base para seleccionar resultados de búsqueda con tipo calculado
     */
    private static final String SEARCH_SELECT =
        "SELECT " +
        "  LPAD(coddepto::TEXT, 2, '0') || LPAD(COALESCE(codmipio, 0)::TEXT, 3, '0') || " +
        "    LPAD(COALESCE(codzona, 0)::TEXT, 2, '0') || LPAD(COALESCE(codpuesto, '00'), 2, '0') AS codigo_divipol, " +
        "  CASE clase " +
        "    WHEN 'N' THEN 'PAIS' " +
        "    WHEN 'D' THEN 'DEPTO' " +
        "    WHEN 'M' THEN 'MPIO' " +
        "    WHEN 'Z' THEN 'ZONA' " +
        "    WHEN 'P' THEN 'PUESTO' " +
        "    ELSE 'PUESTO' " +
        "  END AS tipo, " +
        "  coddepto, codmipio, codzona, codpuesto, " +
        "  nomdepto, nommipio, nompuesto, " +
        "  pottotal AS potencial_total, nummesas AS mesas " +
        "FROM divipol ";

    /**
     * Mapea una fila de resultado de búsqueda a DivipolSearchResultDTO
     */
    private DivipolSearchResultDTO mapSearchResult(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        String tipoStr = row.get("tipo", String.class);
        DivipolSearchResultDTO.TipoEnum tipo = null;
        if (tipoStr != null) {
            try {
                tipo = DivipolSearchResultDTO.TipoEnum.fromValue(tipoStr);
            } catch (IllegalArgumentException e) {
                tipo = DivipolSearchResultDTO.TipoEnum.PUESTO;
            }
        }

        return new DivipolSearchResultDTO()
            .codigoDivipol(row.get("codigo_divipol", String.class))
            .tipo(tipo)
            .coddepto(row.get("coddepto", Integer.class))
            .codmipio(row.get("codmipio", Integer.class))
            .codzona(row.get("codzona", Integer.class))
            .codpuesto(row.get("codpuesto", String.class))
            .nomdepto(row.get("nomdepto", String.class))
            .nommipio(row.get("nommipio", String.class))
            .nompuesto(row.get("nompuesto", String.class))
            .potencialTotal(row.get("potencial_total", Long.class))
            .mesas(row.get("mesas", Long.class));
    }

    @Override
    public Flux<DivipolSearchResultDTO> searchByName(String query, int page, int size) {
        String sql =
            SEARCH_SELECT +
            "WHERE search_vector @@ plainto_tsquery('spanish', :query) " +
            "ORDER BY coddepto, codmipio, codzona, codpuesto " +
            "LIMIT :limit OFFSET :offset";

        return databaseClient
            .sql(sql)
            .bind("query", query)
            .bind("limit", size)
            .bind("offset", page * size)
            .map(this::mapSearchResult)
            .all();
    }

    @Override
    public Mono<Long> countSearchByName(String query) {
        String sql = "SELECT COUNT(*) AS total FROM divipol " + "WHERE search_vector @@ plainto_tsquery('spanish', :query)";

        return databaseClient.sql(sql).bind("query", query).map(row -> row.get("total", Long.class)).one().defaultIfEmpty(0L);
    }

    @Override
    public Flux<DivipolSearchResultDTO> searchByCode(String codePrefix, int page, int size) {
        // Construir condición basada en la longitud del prefijo
        String whereClause = buildCodeWhereClause(codePrefix);

        String sql = SEARCH_SELECT + whereClause + " ORDER BY coddepto, codmipio, codzona, codpuesto " + "LIMIT :limit OFFSET :offset";

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        spec = bindCodeParameters(spec, codePrefix);
        spec = spec.bind("limit", size).bind("offset", page * size);

        return spec.map(this::mapSearchResult).all();
    }

    @Override
    public Mono<Long> countSearchByCode(String codePrefix) {
        String whereClause = buildCodeWhereClause(codePrefix);

        String sql = "SELECT COUNT(*) AS total FROM divipol " + whereClause;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        spec = bindCodeParameters(spec, codePrefix);

        return spec.map(row -> row.get("total", Long.class)).one().defaultIfEmpty(0L);
    }

    @Override
    public Flux<DivipolSearchResultDTO> getSuggestionsByName(String query, int limit) {
        String sql =
            SEARCH_SELECT +
            "WHERE search_vector @@ plainto_tsquery('spanish', :query) " +
            "ORDER BY ts_rank(search_vector, plainto_tsquery('spanish', :query)) DESC " +
            "LIMIT :limit";

        return databaseClient.sql(sql).bind("query", query).bind("limit", limit).map(this::mapSearchResult).all();
    }

    @Override
    public Flux<DivipolSearchResultDTO> getSuggestionsByCode(String codePrefix, int limit) {
        String whereClause = buildCodeWhereClause(codePrefix);

        String sql = SEARCH_SELECT + whereClause + " ORDER BY coddepto, codmipio, codzona, codpuesto " + "LIMIT :limit";

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        spec = bindCodeParameters(spec, codePrefix);
        spec = spec.bind("limit", limit);

        return spec.map(this::mapSearchResult).all();
    }

    /**
     * Construye la cláusula WHERE basada en el código divipol ingresado.
     * Formato: DD MMM ZZ PP (2+3+2+2 = 9 caracteres, PP puede ser alfanumérico)
     */
    private String buildCodeWhereClause(String codePrefix) {
        if (codePrefix == null || codePrefix.isEmpty()) {
            return "WHERE 1=1";
        }

        // Normalizar: mayúsculas y solo alfanuméricos
        String cleanCode = codePrefix.toUpperCase().replaceAll("[^0-9A-Z]", "");

        if (cleanCode.isEmpty()) {
            return "WHERE 1=1";
        }

        // Extraer partes numéricas (primeros 7 caracteres deben ser dígitos)
        String numericPart = cleanCode.length() >= 7 ? cleanCode.substring(0, 7) : cleanCode;
        numericPart = numericPart.replaceAll("[^0-9]", "");

        if (numericPart.isEmpty()) {
            return "WHERE 1=1";
        }

        StringBuilder where = new StringBuilder("WHERE ");

        // Departamento (2 dígitos)
        if (numericPart.length() >= 2) {
            int codDepto = Integer.parseInt(numericPart.substring(0, 2));
            if (codDepto > 0) {
                where.append("coddepto = :coddepto");

                // Municipio (3 dígitos, posiciones 2-4)
                if (numericPart.length() >= 5) {
                    int codMpio = Integer.parseInt(numericPart.substring(2, 5));
                    if (codMpio > 0) {
                        where.append(" AND codmipio = :codmipio");

                        // Zona (2 dígitos, posiciones 5-6)
                        if (numericPart.length() >= 7) {
                            int codZona = Integer.parseInt(numericPart.substring(5, 7));
                            if (codZona > 0) {
                                where.append(" AND codzona = :codzona");

                                // Puesto (2 caracteres alfanuméricos, posiciones 7-8)
                                if (cleanCode.length() >= 9) {
                                    String codPuesto = cleanCode.substring(7, 9);
                                    if (!codPuesto.equals("00")) {
                                        where.append(" AND codpuesto = :codpuesto");
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                return "WHERE 1=1"; // Departamento 00 devuelve todo
            }
        }

        return where.toString();
    }

    /**
     * Vincula los parámetros del código a la consulta
     */
    private DatabaseClient.GenericExecuteSpec bindCodeParameters(DatabaseClient.GenericExecuteSpec spec, String codePrefix) {
        if (codePrefix == null || codePrefix.isEmpty()) {
            return spec;
        }

        // Normalizar: mayúsculas y solo alfanuméricos
        String cleanCode = codePrefix.toUpperCase().replaceAll("[^0-9A-Z]", "");

        // Extraer partes numéricas (primeros 7 caracteres deben ser dígitos)
        String numericPart = cleanCode.length() >= 7 ? cleanCode.substring(0, 7) : cleanCode;
        numericPart = numericPart.replaceAll("[^0-9]", "");

        if (numericPart.length() >= 2) {
            int codDepto = Integer.parseInt(numericPart.substring(0, 2));
            if (codDepto > 0) {
                spec = spec.bind("coddepto", codDepto);

                if (numericPart.length() >= 5) {
                    int codMpio = Integer.parseInt(numericPart.substring(2, 5));
                    if (codMpio > 0) {
                        spec = spec.bind("codmipio", codMpio);

                        if (numericPart.length() >= 7) {
                            int codZona = Integer.parseInt(numericPart.substring(5, 7));
                            if (codZona > 0) {
                                spec = spec.bind("codzona", codZona);

                                // Puesto alfanumérico
                                if (cleanCode.length() >= 9) {
                                    String codPuesto = cleanCode.substring(7, 9);
                                    if (!codPuesto.equals("00")) {
                                        spec = spec.bind("codpuesto", codPuesto);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return spec;
    }
}

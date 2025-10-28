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
}

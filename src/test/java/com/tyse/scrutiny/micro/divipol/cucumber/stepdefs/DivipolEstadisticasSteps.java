package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.service.api.dto.DivipolStatsDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Step definitions específicos para escenarios de consulta de estadísticas.
 *
 * Esta clase solo define steps específicos de estadísticas.
 * Los steps comunes están en DivipolCommonSteps.
 */
public class DivipolEstadisticasSteps extends StepDefs {

    private DivipolStatsDTO estadisticas;

    // ===== STEPS ESPECÍFICOS DE ESTADÍSTICAS =====

    @Dado("que la base de datos tiene {int} registros")
    public void queBaseDeDatosTieneRegistros(int cantidad) {
        // Step documentativo - la base de datos ya tiene los datos cargados por Liquibase
        assertThat(cantidad).isGreaterThan(0);
    }

    @Y("las estadísticas contienen:")
    public void lasEstadisticasContienen(DataTable dataTable) {
        estadisticas = actions.returnResult(DivipolStatsDTO.class).getResponseBody().blockFirst(Duration.ofSeconds(10));

        assertThat(estadisticas).isNotNull();

        List<Map<String, String>> validaciones = dataTable.asMaps(String.class, String.class);

        validaciones.forEach(validacion -> {
            String campo = validacion.get("campo");
            String regla = validacion.get("validacion");

            switch (campo) {
                case "totalDepartamentos":
                    if (regla.startsWith("mayor a ")) {
                        int valor = Integer.parseInt(regla.replace("mayor a ", ""));
                        assertThat(estadisticas.getTotalDepartamentos()).isGreaterThan(valor);
                    }
                    break;
                case "totalMunicipios":
                    if (regla.startsWith("mayor a ")) {
                        int valor = Integer.parseInt(regla.replace("mayor a ", ""));
                        assertThat(estadisticas.getTotalMunicipios()).isGreaterThan(valor);
                    }
                    break;
                case "totalZonas":
                    if (regla.startsWith("mayor a ")) {
                        int valor = Integer.parseInt(regla.replace("mayor a ", ""));
                        assertThat(estadisticas.getTotalZonas()).isGreaterThan(valor);
                    }
                    break;
                case "totalPuestos":
                    if (regla.startsWith("mayor a ")) {
                        int valor = Integer.parseInt(regla.replace("mayor a ", ""));
                        assertThat(estadisticas.getTotalPuestos()).isGreaterThan(valor);
                    }
                    break;
                case "potencialTotal":
                    if (regla.startsWith("mayor a ")) {
                        long valor = Long.parseLong(regla.replace("mayor a ", ""));
                        assertThat(estadisticas.getPotencialTotal()).isGreaterThan(valor);
                    }
                    break;
                case "potencialFemenino":
                    if (regla.startsWith("mayor a ")) {
                        long valor = Long.parseLong(regla.replace("mayor a ", ""));
                        assertThat(estadisticas.getPotencialFemenino()).isGreaterThan(valor);
                    }
                    break;
                case "potencialMasculino":
                    if (regla.startsWith("mayor a ")) {
                        long valor = Long.parseLong(regla.replace("mayor a ", ""));
                        assertThat(estadisticas.getPotencialMasculino()).isGreaterThan(valor);
                    }
                    break;
                case "totalMesas":
                    if (regla.startsWith("mayor a ")) {
                        long valor = Long.parseLong(regla.replace("mayor a ", ""));
                        assertThat(estadisticas.getTotalMesas()).isGreaterThan(valor);
                    }
                    break;
            }
        });
    }

    @Y("el potencial total es la suma de femenino y masculino")
    public void elPotencialTotalEsLaSumaDeFemeninoYMasculino() {
        if (estadisticas == null) {
            estadisticas = actions.returnResult(DivipolStatsDTO.class).getResponseBody().blockFirst(Duration.ofSeconds(10));
        }

        assertThat(estadisticas).isNotNull();

        long sumaPotenciales = estadisticas.getPotencialFemenino() + estadisticas.getPotencialMasculino();
        assertThat(estadisticas.getPotencialTotal())
            .as("El potencial total debe ser igual a la suma de femenino y masculino")
            .isEqualTo(sumaPotenciales);
    }

    @Y("las estadísticas muestran:")
    public void lasEstadisticasMuestran(DataTable dataTable) {
        if (estadisticas == null) {
            estadisticas = actions.returnResult(DivipolStatsDTO.class).getResponseBody().blockFirst(Duration.ofSeconds(10));
        }

        assertThat(estadisticas).isNotNull();

        List<Map<String, String>> validaciones = dataTable.asMaps(String.class, String.class);

        validaciones.forEach(validacion -> {
            String campo = validacion.get("campo");
            String valor = validacion.get("valor");

            switch (campo) {
                case "totalDepartamentos":
                    if (valor.startsWith("> ")) {
                        int valorEsperado = Integer.parseInt(valor.replace("> ", ""));
                        assertThat(estadisticas.getTotalDepartamentos()).isGreaterThan(valorEsperado);
                    } else if (valor.startsWith(">= ")) {
                        int valorEsperado = Integer.parseInt(valor.replace(">= ", ""));
                        assertThat(estadisticas.getTotalDepartamentos()).isGreaterThanOrEqualTo(valorEsperado);
                    } else {
                        assertThat(estadisticas.getTotalDepartamentos()).isEqualTo(Integer.parseInt(valor));
                    }
                    break;
                case "totalMunicipios":
                    if (valor.startsWith("> ")) {
                        int valorEsperado = Integer.parseInt(valor.replace("> ", ""));
                        assertThat(estadisticas.getTotalMunicipios()).isGreaterThan(valorEsperado);
                    } else if (valor.startsWith(">= ")) {
                        int valorEsperado = Integer.parseInt(valor.replace(">= ", ""));
                        assertThat(estadisticas.getTotalMunicipios()).isGreaterThanOrEqualTo(valorEsperado);
                    } else {
                        assertThat(estadisticas.getTotalMunicipios()).isEqualTo(Integer.parseInt(valor));
                    }
                    break;
                case "totalZonas":
                    if (valor.startsWith("> ")) {
                        int valorEsperado = Integer.parseInt(valor.replace("> ", ""));
                        assertThat(estadisticas.getTotalZonas()).isGreaterThan(valorEsperado);
                    } else if (valor.startsWith(">= ")) {
                        int valorEsperado = Integer.parseInt(valor.replace(">= ", ""));
                        assertThat(estadisticas.getTotalZonas()).isGreaterThanOrEqualTo(valorEsperado);
                    } else {
                        assertThat(estadisticas.getTotalZonas()).isEqualTo(Integer.parseInt(valor));
                    }
                    break;
            }
        });
    }

    @Y("todas las estadísticas son cero")
    public void todasLasEstadisticasSonCero() {
        if (estadisticas == null) {
            estadisticas = actions.returnResult(DivipolStatsDTO.class).getResponseBody().blockFirst(Duration.ofSeconds(10));
        }

        assertThat(estadisticas).isNotNull();
        assertThat(estadisticas.getTotalDepartamentos()).isEqualTo(0);
        assertThat(estadisticas.getTotalMunicipios()).isEqualTo(0);
        assertThat(estadisticas.getTotalZonas()).isEqualTo(0);
        assertThat(estadisticas.getTotalPuestos()).isEqualTo(0);
        assertThat(estadisticas.getPotencialTotal()).isEqualTo(0L);
        assertThat(estadisticas.getPotencialFemenino()).isEqualTo(0L);
        assertThat(estadisticas.getPotencialMasculino()).isEqualTo(0L);
        assertThat(estadisticas.getTotalMesas()).isEqualTo(0L);
    }
}

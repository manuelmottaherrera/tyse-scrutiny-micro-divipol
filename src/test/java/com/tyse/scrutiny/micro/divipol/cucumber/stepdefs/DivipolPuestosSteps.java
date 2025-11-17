package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.service.api.dto.DivipolPuestoDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import java.time.Duration;
import java.util.List;

/**
 * Step definitions específicos para escenarios de consulta de puestos.
 *
 * Esta clase solo define steps específicos de puestos.
 * Los steps comunes están en DivipolCommonSteps.
 */
public class DivipolPuestosSteps extends StepDefs {

    private List<DivipolPuestoDTO> puestos;

    // ===== STEPS ESPECÍFICOS DE PUESTOS =====

    @Y("la respuesta contiene {int} puestos")
    public void laRespuestaContienePuestos(int cantidad) {
        puestos = actions.returnResult(DivipolPuestoDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));

        assertThat(puestos).isNotNull();
        assertThat(puestos).hasSize(cantidad);
    }

    @Y("cada puesto tiene los siguientes campos:")
    public void cadaPuestoTieneLosSiguientesCampos(DataTable dataTable) {
        if (puestos == null) {
            puestos = actions.returnResult(DivipolPuestoDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));
        }

        assertThat(puestos).isNotNull();

        // Si hay puestos, validar campos
        if (!puestos.isEmpty()) {
            List<String> campos = dataTable.asList(String.class);

            puestos.forEach(puesto -> {
                if (campos.contains("coddepto")) {
                    assertThat(puesto.getCoddepto()).as("Campo coddepto debe existir").isNotNull();
                }
                if (campos.contains("codmipio")) {
                    assertThat(puesto.getCodmipio()).as("Campo codmipio debe existir").isNotNull();
                }
                if (campos.contains("codzona")) {
                    assertThat(puesto.getCodzona()).as("Campo codzona debe existir").isNotNull();
                }
                if (campos.contains("codpuesto")) {
                    assertThat(puesto.getCodpuesto()).as("Campo codpuesto debe existir").isNotNull();
                }
                if (campos.contains("nompuesto")) {
                    assertThat(puesto.getNompuesto()).as("Campo nompuesto debe existir").isNotBlank();
                }
            });
        }
    }
}

package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.service.api.dto.DivipolZonaDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import java.time.Duration;
import java.util.List;

/**
 * Step definitions específicos para escenarios de consulta de zonas.
 *
 * Esta clase solo define steps específicos de zonas.
 * Los steps comunes están en DivipolCommonSteps.
 */
public class DivipolZonasSteps extends StepDefs {

    private List<DivipolZonaDTO> zonas;

    // ===== STEPS ESPECÍFICOS DE ZONAS =====

    @Y("la respuesta contiene {int} zonas")
    public void laRespuestaContieneZonas(int cantidad) {
        zonas = actions.returnResult(DivipolZonaDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));

        assertThat(zonas).isNotNull();
        assertThat(zonas).hasSize(cantidad);
    }

    @Y("cada zona tiene los siguientes campos:")
    public void cadaZonaTieneLosSiguientesCampos(DataTable dataTable) {
        if (zonas == null) {
            zonas = actions.returnResult(DivipolZonaDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));
        }

        assertThat(zonas).isNotNull();

        // Si hay zonas, validar campos
        if (!zonas.isEmpty()) {
            List<String> campos = dataTable.asList(String.class);

            zonas.forEach(zona -> {
                if (campos.contains("coddepto")) {
                    assertThat(zona.getCoddepto()).as("Campo coddepto debe existir").isNotNull();
                }
                if (campos.contains("codmipio")) {
                    assertThat(zona.getCodmipio()).as("Campo codmipio debe existir").isNotNull();
                }
                if (campos.contains("codzona")) {
                    assertThat(zona.getCodzona()).as("Campo codzona debe existir").isNotNull();
                }
                if (campos.contains("nomdepto")) {
                    assertThat(zona.getNomdepto()).as("Campo nomdepto debe existir").isNotBlank();
                }
                if (campos.contains("nommipio")) {
                    assertThat(zona.getNommipio()).as("Campo nommipio debe existir").isNotBlank();
                }
            });
        }
    }
}

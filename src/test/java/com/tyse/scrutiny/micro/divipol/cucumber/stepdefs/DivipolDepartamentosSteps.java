package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.service.api.dto.DivipolDepartamentoDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;

/**
 * Step definitions específicos para escenarios de consulta de departamentos.
 *
 * Esta clase solo define steps específicos de departamentos.
 * Los steps comunes están en DivipolCommonSteps.
 */
public class DivipolDepartamentosSteps extends StepDefs {

    private List<DivipolDepartamentoDTO> departamentos;

    // ===== STEPS ESPECÍFICOS DE DEPARTAMENTOS =====

    @Y("el content-type de la respuesta es {string}")
    public void elContentTypeDeLaRespuestaEs(String contentType) {
        actions.expectHeader().contentType(MediaType.parseMediaType(contentType));
    }

    @Y("la respuesta contiene al menos {int} departamentos")
    public void laRespuestaContieneAlMenosDepartamentos(int cantidadMinima) {
        departamentos = actions.returnResult(DivipolDepartamentoDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));

        assertThat(departamentos).isNotNull();
        assertThat(departamentos.size()).isGreaterThanOrEqualTo(cantidadMinima);
    }

    @Y("cada departamento tiene los siguientes campos obligatorios:")
    public void cadaDepartamentoTieneLosSiguientesCamposObligatorios(DataTable dataTable) {
        assertThat(departamentos).isNotNull();
        assertThat(departamentos).isNotEmpty();

        List<String> campos = dataTable.asList(String.class);

        departamentos.forEach(depto -> {
            if (campos.contains("coddepto")) {
                assertThat(depto.getCoddepto()).as("Campo coddepto debe existir").isNotNull();
            }
            if (campos.contains("nomdepto")) {
                assertThat(depto.getNomdepto()).as("Campo nomdepto debe existir").isNotBlank();
            }
            if (campos.contains("totalPotencial")) {
                assertThat(depto.getTotalPotencial()).as("Campo totalPotencial debe existir").isNotNull();
            }
            if (campos.contains("mesas")) {
                assertThat(depto.getMesas()).as("Campo mesas debe existir").isNotNull();
            }
            if (campos.contains("mujeres")) {
                assertThat(depto.getMujeres()).as("Campo mujeres debe existir").isNotNull();
            }
            if (campos.contains("hombres")) {
                assertThat(depto.getHombres()).as("Campo hombres debe existir").isNotNull();
            }
        });
    }

    @Y("cada departamento cumple la regla: totalPotencial = mujeres + hombres")
    public void cadaDepartamentoCumpleLaReglaTotalPotencialMujeresHombres() {
        // Primero obtener los departamentos si no se han obtenido aún
        if (departamentos == null || departamentos.isEmpty()) {
            departamentos = actions
                .returnResult(DivipolDepartamentoDTO.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(10));
        }

        assertThat(departamentos).isNotNull();
        assertThat(departamentos).isNotEmpty();

        departamentos.forEach(depto -> {
            Long totalCalculado = depto.getMujeres() + depto.getHombres();
            assertThat(depto.getTotalPotencial())
                .as("El totalPotencial de %s debe ser igual a mujeres + hombres", depto.getNomdepto())
                .isEqualTo(totalCalculado);
        });
    }

    @Y("todos los departamentos tienen:")
    public void todosLosDepartamentosTienen(DataTable dataTable) {
        if (departamentos == null || departamentos.isEmpty()) {
            departamentos = actions
                .returnResult(DivipolDepartamentoDTO.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(10));
        }

        assertThat(departamentos).isNotNull();
        assertThat(departamentos).isNotEmpty();

        List<Map<String, String>> validaciones = dataTable.asMaps(String.class, String.class);

        departamentos.forEach(depto -> {
            validaciones.forEach(validacion -> {
                String campo = validacion.get("campo");
                String regla = validacion.get("validacion");

                switch (campo) {
                    case "coddepto":
                        if ("mayor a 0".equals(regla)) {
                            assertThat(depto.getCoddepto()).isGreaterThan(0);
                        }
                        break;
                    case "totalPotencial":
                        if ("mayor o igual a 0".equals(regla)) {
                            assertThat(depto.getTotalPotencial()).isGreaterThanOrEqualTo(0L);
                        }
                        break;
                    case "mesas":
                        if ("mayor o igual a 0".equals(regla)) {
                            assertThat(depto.getMesas()).isGreaterThanOrEqualTo(0L);
                        }
                        break;
                    case "mujeres":
                        if ("mayor o igual a 0".equals(regla)) {
                            assertThat(depto.getMujeres()).isGreaterThanOrEqualTo(0L);
                        }
                        break;
                    case "hombres":
                        if ("mayor o igual a 0".equals(regla)) {
                            assertThat(depto.getHombres()).isGreaterThanOrEqualTo(0L);
                        }
                        break;
                }
            });
        });
    }

    @Y("al menos un departamento se llama {string}")
    public void alMenosUnDepartamentoSeLlama(String nombreDepartamento) {
        if (departamentos == null || departamentos.isEmpty()) {
            departamentos = actions
                .returnResult(DivipolDepartamentoDTO.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(10));
        }

        assertThat(departamentos).isNotNull();
        assertThat(departamentos)
            .as("Debe existir al menos un departamento llamado %s", nombreDepartamento)
            .anyMatch(depto -> nombreDepartamento.equals(depto.getNomdepto()));
    }
}

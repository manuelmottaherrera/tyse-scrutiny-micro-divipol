package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.service.api.dto.DivipolMunicipioDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Step definitions específicos para escenarios de consulta de municipios.
 *
 * Esta clase solo define steps específicos de municipios.
 * Los steps comunes están en DivipolCommonSteps.
 */
public class DivipolMunicipiosSteps extends StepDefs {

    private List<DivipolMunicipioDTO> municipios;

    // ===== STEPS ESPECÍFICOS DE MUNICIPIOS =====

    @Y("la respuesta contiene municipios")
    public void laRespuestaContieneMunicipios() {
        municipios = actions.returnResult(DivipolMunicipioDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));

        assertThat(municipios).isNotNull();
        assertThat(municipios).isNotEmpty();
    }

    @Y("la respuesta contiene {int} municipios")
    public void laRespuestaContieneMunicipios(int cantidad) {
        municipios = actions.returnResult(DivipolMunicipioDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));

        assertThat(municipios).isNotNull();
        assertThat(municipios).hasSize(cantidad);
    }

    @Y("cada municipio pertenece al departamento {int}")
    public void cadaMunicipioPertenAlDepartamento(int codDepto) {
        if (municipios == null || municipios.isEmpty()) {
            municipios = actions.returnResult(DivipolMunicipioDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));
        }

        assertThat(municipios).isNotNull();
        assertThat(municipios).isNotEmpty();

        municipios.forEach(municipio -> {
            assertThat(municipio.getCoddepto())
                .as("El municipio %s debe pertenecer al departamento %d", municipio.getNommipio(), codDepto)
                .isEqualTo(codDepto);
        });
    }

    @Y("cada municipio tiene los siguientes campos:")
    public void cadaMunicipioTieneLosSiguientesCampos(DataTable dataTable) {
        if (municipios == null || municipios.isEmpty()) {
            municipios = actions.returnResult(DivipolMunicipioDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));
        }

        assertThat(municipios).isNotNull();
        assertThat(municipios).isNotEmpty();

        List<String> campos = dataTable.asList(String.class);

        municipios.forEach(municipio -> {
            if (campos.contains("coddepto")) {
                assertThat(municipio.getCoddepto()).as("Campo coddepto debe existir").isNotNull();
            }
            if (campos.contains("codmipio")) {
                assertThat(municipio.getCodmipio()).as("Campo codmipio debe existir").isNotNull();
            }
            if (campos.contains("nomdepto")) {
                assertThat(municipio.getNomdepto()).as("Campo nomdepto debe existir").isNotBlank();
            }
            if (campos.contains("nommipio")) {
                assertThat(municipio.getNommipio()).as("Campo nommipio debe existir").isNotBlank();
            }
            if (campos.contains("potencialTotal")) {
                assertThat(municipio.getPotencialTotal()).as("Campo potencialTotal debe existir").isNotNull();
            }
            if (campos.contains("potencialFemenino")) {
                assertThat(municipio.getPotencialFemenino()).as("Campo potencialFemenino debe existir").isNotNull();
            }
            if (campos.contains("potencialMasculino")) {
                assertThat(municipio.getPotencialMasculino()).as("Campo potencialMasculino debe existir").isNotNull();
            }
            if (campos.contains("mesas")) {
                assertThat(municipio.getMesas()).as("Campo mesas debe existir").isNotNull();
            }
        });
    }

    @Y("todos los municipios tienen coddepto igual a {int}")
    public void todosLosMunicipiosTienenCoddeptoIgualA(int codDepto) {
        if (municipios == null || municipios.isEmpty()) {
            municipios = actions.returnResult(DivipolMunicipioDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));
        }

        assertThat(municipios).isNotNull();
        assertThat(municipios).isNotEmpty();

        municipios.forEach(municipio -> {
            assertThat(municipio.getCoddepto()).isEqualTo(codDepto);
        });
    }

    @Y("todos los municipios tienen nomdepto igual a {string}")
    public void todosLosMunicipiosTienenNomdeptoIgualA(String nomDepto) {
        if (municipios == null || municipios.isEmpty()) {
            municipios = actions.returnResult(DivipolMunicipioDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));
        }

        assertThat(municipios).isNotNull();
        assertThat(municipios).isNotEmpty();

        municipios.forEach(municipio -> {
            assertThat(municipio.getNomdepto()).isEqualTo(nomDepto);
        });
    }

    @Y("cada municipio cumple la regla: potencialTotal = potencialFemenino + potencialMasculino")
    public void cadaMunicipioCumpleLaReglaPotencialTotal() {
        if (municipios == null || municipios.isEmpty()) {
            municipios = actions.returnResult(DivipolMunicipioDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));
        }

        assertThat(municipios).isNotNull();
        assertThat(municipios).isNotEmpty();

        municipios.forEach(municipio -> {
            Long totalCalculado = municipio.getPotencialFemenino() + municipio.getPotencialMasculino();
            assertThat(municipio.getPotencialTotal())
                .as("El potencialTotal de %s debe ser igual a potencialFemenino + potencialMasculino", municipio.getNommipio())
                .isEqualTo(totalCalculado);
        });
    }

    @Y("todos los municipios tienen:")
    public void todosLosMunicipiosTienen(DataTable dataTable) {
        if (municipios == null || municipios.isEmpty()) {
            municipios = actions.returnResult(DivipolMunicipioDTO.class).getResponseBody().collectList().block(Duration.ofSeconds(10));
        }

        assertThat(municipios).isNotNull();
        assertThat(municipios).isNotEmpty();

        List<Map<String, String>> validaciones = dataTable.asMaps(String.class, String.class);

        municipios.forEach(municipio -> {
            validaciones.forEach(validacion -> {
                String campo = validacion.get("campo");
                String regla = validacion.get("validacion");

                switch (campo) {
                    case "codmipio":
                        if ("mayor a 0".equals(regla)) {
                            assertThat(municipio.getCodmipio()).isGreaterThan(0);
                        }
                        break;
                    case "potencialTotal":
                        if ("mayor o igual a 0".equals(regla)) {
                            assertThat(municipio.getPotencialTotal()).isGreaterThanOrEqualTo(0L);
                        }
                        break;
                    case "mesas":
                        if ("mayor o igual a 0".equals(regla)) {
                            assertThat(municipio.getMesas()).isGreaterThanOrEqualTo(0L);
                        }
                        break;
                }
            });
        });
    }
}

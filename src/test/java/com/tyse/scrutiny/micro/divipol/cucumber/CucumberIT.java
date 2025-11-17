package com.tyse.scrutiny.micro.divipol.cucumber;

import com.tyse.scrutiny.micro.divipol.IntegrationTest;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Runner de Cucumber para ejecutar tests BDD.
 *
 * Este runner ejecuta todos los archivos .feature ubicados en src/test/resources/features/
 * y los vincula con los step definitions en el paquete cucumber.stepdefs
 *
 * Configuración:
 * - Features: src/test/resources/features/
 * - Step Definitions: com.tyse.scrutiny.micro.divipol.cucumber.stepdefs
 * - Reportes: target/cucumber-reports/
 *
 * Ejecutar todos los tests:
 *   ./mvnw verify
 *
 * Ejecutar solo Cucumber:
 *   ./mvnw verify -Dcucumber.filter.tags="@departamentos"
 *
 * Ejecutar solo smoke tests:
 *   ./mvnw verify -Dcucumber.filter.tags="@smoke"
 *
 * Excluir tests específicos:
 *   ./mvnw verify -Dcucumber.filter.tags="not @wip"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.tyse.scrutiny.micro.divipol.cucumber.stepdefs")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, " +
            "html:target/cucumber-reports/cucumber.html, " +
            "json:target/cucumber-reports/cucumber.json, " +
            "junit:target/cucumber-reports/cucumber.xml"
)
@IntegrationTest
class CucumberIT {}

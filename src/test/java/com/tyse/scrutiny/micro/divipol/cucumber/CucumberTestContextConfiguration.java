package com.tyse.scrutiny.micro.divipol.cucumber;

import com.tyse.scrutiny.micro.divipol.IntegrationTest;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Configuración de Spring para Cucumber.
 *
 * Esta clase configura el contexto de Spring para que Cucumber pueda:
 * - Cargar el contexto completo de la aplicación (@IntegrationTest)
 * - Inyectar beans de Spring en los step definitions
 * - Usar WebTestClient para llamar a los endpoints
 * - Acceder a Testcontainers (PostgreSQL, Kafka)
 *
 * NOTA: No se usa @WithMockUser a nivel de clase para permitir que cada escenario
 * maneje su propia autenticación usando tokens JWT válidos.
 */
@CucumberContextConfiguration
@IntegrationTest
@WebAppConfiguration
@AutoConfigureWebTestClient(timeout = "PT10S")
public class CucumberTestContextConfiguration {}

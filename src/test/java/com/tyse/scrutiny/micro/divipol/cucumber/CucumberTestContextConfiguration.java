package com.tyse.scrutiny.micro.divipol.cucumber;

import com.tyse.scrutiny.micro.divipol.IntegrationTest;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Configuración de Spring para Cucumber.
 *
 * Esta clase configura el contexto de Spring para que Cucumber pueda:
 * - Cargar el contexto completo de la aplicación (@IntegrationTest)
 * - Inyectar beans de Spring en los step definitions
 * - Usar WebTestClient para llamar a los endpoints
 * - Usar autenticación mock (@WithMockUser)
 * - Acceder a Testcontainers (PostgreSQL, Kafka)
 */
@CucumberContextConfiguration
@IntegrationTest
@WebAppConfiguration
@AutoConfigureWebTestClient(timeout = "PT10S")
@WithMockUser
public class CucumberTestContextConfiguration {}

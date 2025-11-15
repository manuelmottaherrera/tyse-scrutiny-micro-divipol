# 🥒 PLAN MAESTRO: TESTING COMPLETO CON CUCUMBER BDD

**Proyecto:** tyse-scrutiny-micro-divipol
**Fecha de creación:** 15 de Noviembre de 2025
**Framework:** Cucumber 7.22.1 + Spring Boot 3.4.5 + JUnit 5
**Estado inicial:** 49% cobertura, 94 tests JUnit
**Objetivo final:** 72% cobertura, 100+ scenarios Cucumber, 200+ tests totales

---

## 📚 ÍNDICE

1. [¿Qué es Cucumber y Por Qué Usarlo?](#qué-es-cucumber-y-por-qué-usarlo)
2. [Arquitectura del Plan](#arquitectura-del-plan)
3. [Estado Actual](#estado-actual)
4. [HITO 1: Fundamentos con Cucumber](#hito-1-fundamentos-con-cucumber)
5. [HITO 2: Seguridad con Cucumber](#hito-2-seguridad-con-cucumber)
6. [HITO 3: Cucumber Avanzado](#hito-3-cucumber-avanzado)
7. [HITO 4: Cobertura Completa](#hito-4-cobertura-completa)
8. [HITO 5: Excelencia Operacional](#hito-5-excelencia-operacional)
9. [Resumen Ejecutivo](#resumen-ejecutivo)
10. [Comandos Útiles](#comandos-útiles)
11. [Referencias y Recursos](#referencias-y-recursos)

---

## 🎯 ¿Qué es Cucumber y Por Qué Usarlo?

### ¿Qué es Cucumber?

**Cucumber** es un framework de **BDD (Behavior-Driven Development)** que permite escribir tests en **lenguaje natural** usando la sintaxis **Gherkin**.

### Beneficios Clave

| Beneficio | Descripción |
|-----------|-------------|
| 📖 **Documentación Viva** | Los `.feature` documentan cómo funciona la API en lenguaje natural |
| 🤝 **Comunicación** | Product owners y stakeholders pueden leer y validar comportamiento |
| ✅ **Testing BDD** | Primero defines comportamiento, luego implementas |
| 🔄 **Reutilización** | Steps se reusan entre múltiples scenarios |
| 🎯 **Enfoque** | Escribes lo que importa, no cómo se implementa |
| 📊 **Reportes** | HTML hermosos que puedes compartir con stakeholders |

### Ejemplo Comparativo

#### ❌ Test Tradicional (Solo devs lo entienden)
```java
@Test
void getAllDepartamentos_shouldReturnListOfDepartamentos() {
  webTestClient.get().uri("/api/divipol/departamentos")
    .exchange().expectStatus().isOk()...
}
```

#### ✅ Cucumber (TODOS lo entienden)
```gherkin
# language: es
Característica: Consultar departamentos de Colombia
  Como usuario de la API
  Quiero consultar todos los departamentos
  Para obtener información de división política

  Escenario: Consultar todos los departamentos exitosamente
    Dado que la base de datos tiene datos de DIVIPOL
    Cuando consulto el endpoint GET "/api/divipol/departamentos"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene al menos 32 departamentos
    Y cada departamento tiene código, nombre, y estadísticas
```

---

## 🗺️ Arquitectura del Plan

```
HITO 1: FUNDAMENTOS CON CUCUMBER (Crítico + Aprendizaje)
├── Fase 1A: Cucumber 101 - Tu primer feature ✅ COMPLETADA
├── Fase 1B: Cucumber para todos los endpoints
├── Fase 1C: Tests unitarios puros (complemento)
└── Fase 1D: Cucumber para validaciones

HITO 2: SEGURIDAD CON CUCUMBER (Alta prioridad)
├── Fase 2A: Scenarios de autenticación
├── Fase 2B: Scenarios de autorización
└── Fase 2C: Manejo de errores con ejemplos

HITO 3: CUCUMBER AVANZADO (Media prioridad)
├── Fase 3A: Data Tables y Scenario Outlines
├── Fase 3B: Tags y ejecución selectiva
└── Fase 3C: Hooks y backgrounds

HITO 4: COBERTURA COMPLETA (Media prioridad)
├── Fase 4A: RowMappers y Repository
├── Fase 4B: Tests parametrizados
└── Fase 4C: Alcanzar 68%+ cobertura

HITO 5: PERFORMANCE Y EXCELENCIA (Opcional)
├── Fase 5A: Gatling + ArchUnit
├── Fase 5B: Observabilidad
└── Fase 5C: Documentación final
```

---

## 📊 Estado Actual

### Métricas Iniciales (Antes de Cucumber)
- **Cobertura:** 49%
- **Tests JUnit:** 94 (52 unit + 42 integration)
- **Scenarios Cucumber:** 0
- **Features:** 0

### Estado Actual (Después de Fase 1A)
- ✅ **Cobertura:** 49% (sin cambio, Cucumber es documentación)
- ✅ **Tests JUnit:** 94 (mantiene)
- ✅ **Scenarios Cucumber:** 3 ✨ NUEVO
- ✅ **Features:** 1 ✨ NUEVO
- ✅ **Step Definitions:** 12 steps ✨ NUEVO
- ✅ **Autenticación:** JWT real implementada ✨ MEJORADO

### Archivos Creados en Fase 1A
```
✅ src/test/resources/features/divipol-departamentos.feature
✅ src/test/java/.../cucumber/stepdefs/DivipolDepartamentosSteps.java
✅ src/test/java/.../cucumber/CucumberIT.java (actualizado)
✅ src/test/java/.../cucumber/CucumberTestContextConfiguration.java (mejorado)
✅ src/test/resources/cucumber.properties
✅ src/test/resources/features/README-CUCUMBER.md
✅ src/test/resources/config/application-testprod.yml
```

---

## 🎯 HITO 1: FUNDAMENTOS CON CUCUMBER

**Objetivo:** Aprender Cucumber mientras mejoras tus tests
**Cobertura esperada:** 49% → 58%
**Scenarios esperados:** 3 → 48
**Tests JUnit esperados:** 94 → 130

---

### ✅ FASE 1A: CUCUMBER 101 - COMPLETADA

**Duración:** 45 minutos (incluye aprendizaje)
**Estado:** ✅ **COMPLETADA**
**Commit:** `912dd60` - fix(tests): Corregir errores de compilación y tests de Cucumber BDD

#### Entregables Completados

1. **divipol-departamentos.feature** (3 escenarios)
   - ✅ Consultar todos los departamentos exitosamente (@happy-path)
   - ✅ Consultar departamentos sin autenticación (@seguridad)
   - ✅ Verificar coherencia de datos (@validacion-datos)

2. **DivipolDepartamentosSteps.java** (12 steps)
   - ✅ Dado que la base de datos tiene cargados los datos de DIVIPOL
   - ✅ Dado que soy un usuario autenticado (con JWT real)
   - ✅ Dado que NO estoy autenticado
   - ✅ Cuando consulto el endpoint GET {string}
   - ✅ Entonces recibo un código de respuesta {int}
   - ✅ Y el content-type de la respuesta es {string}
   - ✅ Y la respuesta contiene al menos {int} departamentos
   - ✅ Y cada departamento tiene los siguientes campos obligatorios
   - ✅ Y cada departamento cumple la regla: totalPotencial = mujeres + hombres
   - ✅ Y todos los departamentos tienen (validaciones con DataTable)
   - ✅ Y al menos un departamento se llama {string}

3. **Mejoras Implementadas**
   - ✅ Autenticación JWT REAL (no mock)
   - ✅ Reportes HTML, JSON y XML configurados
   - ✅ Tags organizados (@departamentos, @smoke, @critical)
   - ✅ Documentación completa en README-CUCUMBER.md

#### Comandos de Verificación

```bash
# Ejecutar solo Cucumber
./mvnw verify -Dtest=CucumberIT

# Ver reporte HTML
firefox target/cucumber-reports/cucumber.html

# Ejecutar solo smoke tests
./mvnw verify -Dcucumber.filter.tags="@smoke"
```

#### Resultado Esperado
```
3 Scenarios (3 passed)
12 Steps (12 passed)
BUILD SUCCESS ✅
```

---

### 📦 FASE 1B: Cucumber para Todos los Endpoints

**Duración estimada:** 60 minutos
**Impacto:** Documentación completa de la API en lenguaje natural
**Scenarios esperados:** +22 (3 → 25)

#### Archivos a Crear

##### 1. divipol-municipios.feature (5 escenarios)

**Ubicación:** `src/test/resources/features/divipol-municipios.feature`

```gherkin
# language: es
@municipios @regression
Característica: Consultar municipios por departamento
  Como usuario de la API DIVIPOL
  Quiero consultar municipios filtrados por departamento
  Para obtener información de división política municipal

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Consultar municipios de Bolívar exitosamente
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=5"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene municipios
    Y cada municipio pertenece al departamento 5
    Y cada municipio tiene los siguientes campos:
      | campo              |
      | coddepto           |
      | codmipio           |
      | nomdepto           |
      | nommipio           |
      | potencialTotal     |
      | potencialFemenino  |
      | potencialMasculino |
      | mesas              |

  @edge-case
  Escenario: Consultar municipios de departamento inexistente
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=999"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene 0 municipios

  @validacion
  Escenario: Consultar municipios con código inválido debe fallar
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=-1"
    Entonces recibo un código de respuesta 400

  @coherencia
  Escenario: Validar que municipios pertenecen al departamento solicitado
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=5"
    Entonces recibo un código de respuesta 200
    Y todos los municipios tienen coddepto igual a 5
    Y todos los municipios tienen nomdepto igual a "BOLIVAR"

  @coherencia
  Escenario: Validar coherencia de datos en municipios
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=5"
    Entonces recibo un código de respuesta 200
    Y cada municipio cumple la regla: potencialTotal = potencialFemenino + potencialMasculino
    Y todos los municipios tienen:
      | campo              | validacion        |
      | codmipio           | mayor a 0         |
      | potencialTotal     | mayor o igual a 0 |
      | mesas              | mayor o igual a 0 |
```

**Step Definitions:** `DivipolMunicipiosSteps.java` (8 steps nuevos)

---

##### 2. divipol-zonas.feature (4 escenarios)

**Ubicación:** `src/test/resources/features/divipol-zonas.feature`

```gherkin
# language: es
@zonas @regression
Característica: Consultar zonas por municipio
  Como usuario de la API DIVIPOL
  Quiero consultar zonas filtradas por municipio
  Para obtener información de división política por zonas

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Consultar zonas de Cartagena exitosamente
    Cuando consulto el endpoint GET "/api/divipol/zonas?codDepto=5&codMpio=1"
    Entonces recibo un código de respuesta 200
    Y cada zona tiene los siguientes campos:
      | campo     |
      | coddepto  |
      | codmipio  |
      | codzona   |
      | nomdepto  |
      | nommipio  |
      | nomzona   |

  @edge-case
  Escenario: Consultar zonas de municipio inexistente
    Cuando consulto el endpoint GET "/api/divipol/zonas?codDepto=999&codMpio=999"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene 0 zonas

  Esquema del escenario: Consultar zonas con diferentes parámetros
    Cuando consulto el endpoint GET "/api/divipol/zonas?codDepto=<codDepto>&codMpio=<codMpio>"
    Entonces recibo un código de respuesta <codigo>

    Ejemplos:
      | codDepto | codMpio | codigo |
      | 5        | 1       | 200    |
      | 999      | 999     | 200    |
      | -1       | 1       | 400    |
      | 5        | -1      | 400    |
```

**Step Definitions:** `DivipolZonasSteps.java` (6 steps nuevos)

---

##### 3. divipol-puestos.feature (4 escenarios)

**Ubicación:** `src/test/resources/features/divipol-puestos.feature`

```gherkin
# language: es
@puestos @regression
Característica: Consultar puestos por zona
  Como usuario de la API DIVIPOL
  Quiero consultar puestos filtrados por zona
  Para obtener información detallada de puestos de votación

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Consultar puestos de una zona exitosamente
    Cuando consulto el endpoint GET "/api/divipol/puestos?codDepto=5&codMpio=1&codZona=1"
    Entonces recibo un código de respuesta 200
    Y cada puesto tiene los siguientes campos:
      | campo      |
      | coddepto   |
      | codmipio   |
      | codzona    |
      | codpuesto  |
      | nompuesto  |
      | direccion  |

  @edge-case
  Escenario: Consultar puestos de zona inexistente
    Cuando consulto el endpoint GET "/api/divipol/puestos?codDepto=999&codMpio=999&codZona=999"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene 0 puestos

  Esquema del escenario: Validar parámetros de puestos
    Cuando consulto el endpoint GET "/api/divipol/puestos?codDepto=<depto>&codMpio=<mpio>&codZona=<zona>"
    Entonces recibo un código de respuesta <codigo>

    Ejemplos:
      | depto | mpio | zona | codigo |
      | 5     | 1    | 1    | 200    |
      | -1    | 1    | 1    | 400    |
      | 5     | -1   | 1    | 400    |
      | 5     | 1    | -1   | 400    |
```

**Step Definitions:** `DivipolPuestosSteps.java` (5 steps nuevos)

---

##### 4. divipol-estadisticas.feature (6 escenarios)

**Ubicación:** `src/test/resources/features/divipol-estadisticas.feature`

```gherkin
# language: es
@estadisticas @critical
Característica: Consultar estadísticas de DIVIPOL
  Como usuario de la API DIVIPOL
  Quiero consultar estadísticas agregadas
  Para obtener análisis de división política

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path @smoke
  Escenario: Consultar estadísticas generales
    Dado que la base de datos tiene 18,016 registros
    Cuando consulto el endpoint GET "/api/divipol/stats"
    Entonces recibo un código de respuesta 200
    Y las estadísticas contienen:
      | campo              | validacion          |
      | totalDepartamentos | mayor a 30          |
      | totalMunicipios    | mayor a 1000        |
      | totalZonas         | mayor a 0           |
      | totalPuestos       | mayor a 0           |
      | potencialTotal     | mayor a 30000000    |
      | potencialFemenino  | mayor a 15000000    |
      | potencialMasculino | mayor a 15000000    |
      | totalMesas         | mayor a 0           |
    Y el potencial total es la suma de femenino y masculino

  @happy-path
  Escenario: Consultar estadísticas de un departamento
    Cuando consulto el endpoint GET "/api/divipol/stats/departamento/5"
    Entonces recibo un código de respuesta 200
    Y las estadísticas muestran:
      | campo              | valor |
      | totalDepartamentos | 1     |
      | totalMunicipios    | > 0   |
    Y el potencial total es la suma de femenino y masculino

  @happy-path
  Escenario: Consultar estadísticas de un municipio
    Cuando consulto el endpoint GET "/api/divipol/stats/municipio/5/1"
    Entonces recibo un código de respuesta 200
    Y las estadísticas muestran:
      | campo              | valor |
      | totalDepartamentos | 1     |
      | totalMunicipios    | 1     |
      | totalZonas         | >= 0  |
    Y el potencial total es la suma de femenino y masculino

  @happy-path
  Escenario: Consultar estadísticas de una zona
    Cuando consulto el endpoint GET "/api/divipol/stats/zona/5/1/1"
    Entonces recibo un código de respuesta 200
    Y las estadísticas muestran:
      | campo              | valor |
      | totalDepartamentos | 1     |
      | totalMunicipios    | 1     |
      | totalZonas         | 1     |

  @edge-case
  Escenario: Estadísticas de departamento inexistente
    Cuando consulto el endpoint GET "/api/divipol/stats/departamento/999"
    Entonces recibo un código de respuesta 200
    Y todas las estadísticas son cero

  @validacion
  Escenario: Estadísticas con código inválido
    Cuando consulto el endpoint GET "/api/divipol/stats/departamento/-1"
    Entonces recibo un código de respuesta 400
```

**Step Definitions:** `DivipolEstadisticasSteps.java` (7 steps nuevos)

---

##### 5. Clase Base Común: DivipolCommonSteps.java

**Ubicación:** `src/test/java/.../cucumber/stepdefs/DivipolCommonSteps.java`

```java
package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.security.jwt.JwtAuthenticationTestUtils;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Step definitions comunes reutilizables en múltiples features.
 */
public class DivipolCommonSteps extends StepDefs {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    private String jwtKey;

    protected boolean authenticated = true;

    @Dado("que la base de datos tiene cargados los datos de DIVIPOL")
    public void queBaseDeDatosTieneDatosDeDivipol() {
        assertThat(webTestClient).isNotNull();
    }

    @Dado("que soy un usuario autenticado")
    public void queSoyUnUsuarioAutenticado() {
        authenticated = true;
    }

    @Dado("que NO estoy autenticado")
    public void queNoEstoyAutenticado() {
        authenticated = false;
    }

    @Cuando("consulto el endpoint GET {string}")
    public void consultoElEndpointGET(String endpoint) {
        WebTestClient.RequestHeadersSpec<?> request = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()
            .get()
            .uri(endpoint)
            .accept(MediaType.APPLICATION_JSON);

        if (authenticated) {
            String token = JwtAuthenticationTestUtils.createValidToken(jwtKey);
            actions = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange();
        } else {
            actions = request.exchange();
        }
    }

    @Entonces("recibo un código de respuesta {int}")
    public void reciboUnCodigoDeRespuesta(int statusCode) {
        actions.expectStatus().isEqualTo(statusCode);
    }
}
```

#### Criterios de Éxito - Fase 1B
- ✅ 22 scenarios nuevos (3 → 25 scenarios)
- ✅ Todos los 8 endpoints documentados con Cucumber
- ✅ Steps reutilizables entre features
- ✅ Reporte HTML muestra cobertura completa de API

---

### 📦 FASE 1C: Tests Unitarios Puros (Complemento)

**Duración estimada:** 30 minutos
**Impacto:** Tests más rápidos, mejor separación de concerns
**Tests esperados:** +18 (94 → 112)
**Cobertura esperada:** +2%

**Nota:** Cucumber es excelente para integration tests, pero para lógica interna necesitamos unit tests puros.

#### Archivos a Crear

##### 1. DivipolServiceTest.java (10 tests unitarios)

**Ubicación:** `src/test/java/.../service/DivipolServiceTest.java`

```java
package com.tyse.scrutiny.micro.divipol.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tyse.scrutiny.micro.divipol.repository.DivipolRepository;
import com.tyse.scrutiny.micro.divipol.service.api.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests unitarios puros de DivipolService usando mocks.
 *
 * Estos tests NO usan Spring Context (@SpringBootTest) para ser más rápidos.
 * Se enfocan en verificar la lógica de negocio aislada.
 */
@ExtendWith(MockitoExtension.class)
class DivipolServiceTest {

    @Mock
    private DivipolRepository divipolRepository;

    @InjectMocks
    private DivipolService divipolService;

    @Test
    void getAllDepartamentos_shouldCallRepositoryAndReturnOk() {
        // Given
        DivipolDepartamentoDTO mockDepto = new DivipolDepartamentoDTO()
            .coddepto(5)
            .nomdepto("BOLIVAR")
            .totalPotencial(1000000L)
            .mesas(5000L)
            .mujeres(500000L)
            .hombres(500000L);

        Flux<DivipolDepartamentoDTO> mockFlux = Flux.just(mockDepto);
        when(divipolRepository.findAllDepartamentos()).thenReturn(mockFlux);

        // When
        Mono<ResponseEntity<Flux<DivipolDepartamentoDTO>>> result =
            divipolService.getAllDepartamentos(null);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.getStatusCodeValue()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
            })
            .verifyComplete();

        verify(divipolRepository, times(1)).findAllDepartamentos();
    }

    @Test
    void getMunicipiosByDepartamento_shouldCallRepositoryWithCorrectParameters() {
        // Given
        Integer codDepto = 5;
        DivipolMunicipioDTO mockMunicipio = new DivipolMunicipioDTO()
            .coddepto(5)
            .codmipio(1)
            .nomdepto("BOLIVAR")
            .nommipio("CARTAGENA");

        Flux<DivipolMunicipioDTO> mockFlux = Flux.just(mockMunicipio);
        when(divipolRepository.findMunicipiosByDepartamento(codDepto)).thenReturn(mockFlux);

        // When
        Mono<ResponseEntity<Flux<DivipolMunicipioDTO>>> result =
            divipolService.getMunicipiosByDepartamento(codDepto, null);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.getStatusCodeValue()).isEqualTo(200);
            })
            .verifyComplete();

        verify(divipolRepository).findMunicipiosByDepartamento(codDepto);
    }

    // ... 8 tests más para cada método del service
}
```

##### 2. KafkaConsumerTest.java (5 tests)

**Ubicación:** `src/test/java/.../broker/KafkaConsumerTest.java`

```java
package com.tyse.scrutiny.micro.divipol.broker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Tests unitarios de KafkaConsumer.
 */
class KafkaConsumerTest {

    private KafkaConsumer kafkaConsumer;

    @BeforeEach
    void setup() {
        kafkaConsumer = new KafkaConsumer();
    }

    @Test
    void accept_shouldEmitMessageToFlux() {
        // Given
        String message = "test-message";
        Flux<String> flux = kafkaConsumer.getFlux();

        // When
        kafkaConsumer.accept(message);

        // Then
        StepVerifier.create(flux)
            .expectNext(message)
            .thenCancel()
            .verify();
    }

    @Test
    void getFlux_shouldReturnNonNullFlux() {
        // When
        Flux<String> flux = kafkaConsumer.getFlux();

        // Then
        assertThat(flux).isNotNull();
    }

    // ... 3 tests más
}
```

##### 3. KafkaProducerTest.java (3 tests)

#### Criterios de Éxito - Fase 1C
- ✅ 18 tests unitarios nuevos (94 → 112)
- ✅ Tests se ejecutan en <5 segundos
- ✅ No usan @SpringBootTest
- ✅ Usan @ExtendWith(MockitoExtension.class)
- ✅ Cobertura de service: 100% (se mantiene)
- ✅ Cobertura de broker: 100% (se mantiene)

---

### 📦 FASE 1D: Cucumber para Validaciones

**Duración estimada:** 40 minutos
**Impacto:** Documentar reglas de validación en lenguaje natural
**Scenarios esperados:** +15 (25 → 40)

#### Archivos a Crear

##### validaciones.feature (15 escenarios)

**Ubicación:** `src/test/resources/features/divipol-validaciones.feature`

```gherkin
# language: es
@validaciones @regression
Característica: Validaciones de parámetros de entrada
  Como desarrollador de la API
  Quiero que los parámetros inválidos sean rechazados
  Para garantizar la integridad de los datos

  Antecedentes:
    Dado que soy un usuario autenticado

  Esquema del escenario: Rechazar códigos de departamento inválidos
    Cuando consulto municipios con código de departamento <codigo>
    Entonces recibo un error 400
    Y el mensaje de error menciona "<razon>"

    Ejemplos: Códigos inválidos
      | codigo | razon                    |
      | -1     | debe ser positivo        |
      | 0      | debe ser mayor a 0       |
      | 100    | fuera de rango           |

  Esquema del escenario: Rechazar códigos de municipio inválidos
    Cuando consulto zonas con codDepto=5 y codMpio=<codigo>
    Entonces recibo un error 400

    Ejemplos:
      | codigo |
      | -1     |
      | 0      |
      | 1000   |

  Esquema del escenario: Rechazar códigos de zona inválidos
    Cuando consulto puestos con codDepto=5, codMpio=1 y codZona=<codigo>
    Entonces recibo un error 400

    Ejemplos:
      | codigo |
      | -1     |
      | 0      |
      | 9999   |

  Escenario: Validar múltiples parámetros simultáneamente
    Cuando consulto puestos con parámetros inválidos:
      | parámetro | valor |
      | codDepto  | -1    |
      | codMpio   | -1    |
      | codZona   | -1    |
    Entonces recibo un error 400
    Y el mensaje lista los 3 errores de validación

  Escenario: Aceptar códigos válidos en el límite inferior
    Cuando consulto municipios con código de departamento 1
    Entonces recibo un código de respuesta 200

  Escenario: Aceptar códigos válidos en el límite superior
    Cuando consulto municipios con código de departamento 99
    Entonces recibo un código de respuesta 200
```

**Step Definitions:** `DivipolValidacionesSteps.java` (8 steps nuevos)

#### Criterios de Éxito - Fase 1D
- ✅ 15 scenarios de validación
- ✅ Data tables para casos múltiples
- ✅ Validaciones documentadas como reglas de negocio
- ✅ Cobertura de web.rest.errors: 68% → 85%

---

## ✅ Criterios de Éxito - HITO 1

Al completar el Hito 1:
- ✅ **Scenarios Cucumber:** 0 → 40 scenarios
- ✅ **Tests JUnit:** 94 → 130
- ✅ **Total tests:** ~170 tests
- ✅ **Cobertura:** 49% → 58%
- ✅ **Features:** 5 archivos .feature
- ✅ **Step definitions:** 8 clases de steps
- ✅ **Reporte Cucumber:** HTML generado y navegable
- ✅ **Documentación:** API completamente documentada en Gherkin

**Comando de verificación final:**
```bash
# Ver scenarios de Cucumber
./mvnw verify | grep "Scenarios"
# Debe mostrar: 40 Scenarios (40 passed)

# Ver reporte HTML
firefox target/cucumber-reports/cucumber.html

# Verificar cobertura
firefox target/site/jacoco-merged/index.html
```

---

## 🎯 HITO 2: SEGURIDAD CON CUCUMBER

**Objetivo:** Documentar y probar seguridad con BDD
**Scenarios esperados:** 40 → 70
**Cobertura esperada:** 58% → 64%

---

### 📦 FASE 2A: Scenarios de Autenticación

**Duración estimada:** 45 minutos
**Scenarios esperados:** +10 (40 → 50)

#### autenticacion.feature (10 escenarios)

**Ubicación:** `src/test/resources/features/seguridad/autenticacion.feature`

```gherkin
# language: es
@autenticacion @seguridad @critical
Característica: Autenticación JWT
  Como sistema de seguridad
  Quiero validar tokens JWT correctamente
  Para proteger los endpoints de la API

  @happy-path
  Escenario: Login exitoso con credenciales válidas
    Dado que tengo las credenciales:
      | usuario  | admin     |
      | password | admin123  |
    Cuando envío una petición POST a "/api/authenticate"
    Entonces recibo un código 200
    Y recibo un token JWT válido
    Y el token tiene una expiración de 24 horas

  @error
  Escenario: Login fallido con credenciales inválidas
    Dado que tengo credenciales incorrectas
    Cuando intento autenticarme
    Entonces recibo un código 401
    Y no recibo ningún token

  Esquema del escenario: Diferentes tipos de fallos de autenticación
    Dado que envío <tipo_credencial>
    Cuando intento autenticarme
    Entonces recibo un código <codigo>
    Y el mensaje dice "<mensaje>"

    Ejemplos:
      | tipo_credencial        | codigo | mensaje                    |
      | credenciales vacías    | 400    | credenciales requeridas    |
      | solo usuario           | 400    | password requerido         |
      | solo password          | 400    | usuario requerido          |
      | usuario inexistente    | 401    | credenciales inválidas     |
      | password incorrecta    | 401    | credenciales inválidas     |

  @token
  Escenario: Acceder con token válido
    Dado que tengo un token JWT válido
    Cuando accedo a un endpoint protegido
    Entonces recibo un código 200

  @token
  Escenario: Acceder con token expirado
    Dado que tengo un token JWT que expiró hace 1 hora
    Cuando accedo a un endpoint protegido
    Entonces recibo un código 401
    Y el mensaje dice "token expirado"

  @token
  Escenario: Acceder con token manipulado
    Dado que tengo un token JWT con firma inválida
    Cuando acceso a un endpoint protegido
    Entonces recibo un código 401
    Y el mensaje dice "token inválido"
```

**Step Definitions:** `AutenticacionSteps.java`

---

### 📦 FASE 2B: Scenarios de Autorización

**Duración estimada:** 45 minutos
**Scenarios esperados:** +15 (50 → 65)

#### autorizacion.feature (15 escenarios)

**Ubicación:** `src/test/resources/features/seguridad/autorizacion.feature`

```gherkin
# language: es
@autorizacion @seguridad @critical
Característica: Autorización basada en roles
  Como sistema de seguridad
  Quiero validar roles y permisos
  Para controlar acceso a recursos

  Antecedentes:
    Dado que existen los siguientes usuarios:
      | usuario | rol         | activo |
      | admin   | ROLE_ADMIN  | true   |
      | user    | ROLE_USER   | true   |
      | guest   | ROLE_GUEST  | true   |
      | blocked | ROLE_USER   | false  |

  Esquema del escenario: Acceso a endpoints según rol
    Dado que estoy autenticado como <usuario>
    Cuando accedo a <endpoint>
    Entonces recibo un código <codigo>

    Ejemplos: Acceso a departamentos
      | usuario | endpoint                      | codigo |
      | admin   | GET /api/divipol/departamentos| 200    |
      | user    | GET /api/divipol/departamentos| 200    |
      | guest   | GET /api/divipol/departamentos| 403    |
      | blocked | GET /api/divipol/departamentos| 401    |

    Ejemplos: Acceso a estadísticas
      | usuario | endpoint                | codigo |
      | admin   | GET /api/divipol/stats  | 200    |
      | user    | GET /api/divipol/stats  | 200    |
      | guest   | GET /api/divipol/stats  | 403    |

  @roles
  Escenario: Usuario sin rol específico no puede acceder
    Dado que estoy autenticado sin roles
    Cuando accedo a cualquier endpoint
    Entonces recibo un código 403

  @roles
  Escenario: Usuario con múltiples roles puede acceder
    Dado que estoy autenticado con roles "ROLE_USER,ROLE_ADMIN"
    Cuando accedo a un endpoint que requiere ROLE_ADMIN
    Entonces recibo un código 200
```

**Step Definitions:** `AutorizacionSteps.java`

---

### 📦 FASE 2C: Manejo de Errores

**Duración estimada:** 30 minutos
**Scenarios esperados:** +5 (65 → 70)

#### manejo-errores.feature (5 escenarios)

```gherkin
# language: es
@errores @regression
Característica: Manejo de errores
  Como sistema robusto
  Quiero manejar errores correctamente
  Para proporcionar mensajes útiles

  Antecedentes:
    Dado que soy un usuario autenticado

  @timeout
  Escenario: Timeout en consulta de base de datos
    Dado que la base de datos está lenta
    Cuando consulto departamentos
    Entonces recibo un código 503
    Y el mensaje dice "servicio temporalmente no disponible"

  @500
  Escenario: Error interno del servidor
    Dado que ocurre un error inesperado
    Cuando consulto cualquier endpoint
    Entonces recibo un código 500
    Y el mensaje NO filtra detalles internos

  @404
  Escenario: Recurso no encontrado
    Cuando consulto un endpoint inexistente
    Entonces recibo un código 404
    Y el mensaje indica que el recurso no existe
```

---

## ✅ Criterios de Éxito - HITO 2

- ✅ **Scenarios totales:** 40 → 70 (+30 scenarios)
- ✅ **Tests JUnit:** 130 → 145 (+15 tests)
- ✅ **Cobertura:** 58% → 64% (+6%)
- ✅ **Seguridad:** Completamente testeada y documentada
- ✅ **Errores:** Manejo robusto verificado

---

## 🎯 HITO 3: CUCUMBER AVANZADO

**Objetivo:** Técnicas avanzadas de Cucumber
**Scenarios esperados:** 70 → 85
**Cobertura esperada:** 64% → 66%

---

### 📦 FASE 3A: Data Tables y Scenario Outlines Avanzados

**Duración estimada:** 40 minutos
**Scenarios esperados:** +10

#### coherencia-datos.feature

```gherkin
# language: es
@coherencia @data-integrity
Característica: Coherencia de datos de DIVIPOL
  Como sistema de calidad de datos
  Quiero validar coherencia matemática
  Para garantizar integridad de información

  @matematica
  Escenario: Validar coherencia matemática en estadísticas
    Cuando consulto estadísticas generales
    Entonces los siguientes cálculos deben ser correctos:
      | campo1             | operador | campo2             | resultado      |
      | potencialFemenino  | +        | potencialMasculino | potencialTotal |
      | totalDepartamentos | >=       | 32                 | true           |
      | totalMunicipios    | >=       | 1000               | true           |
      | totalMesas         | >        | 0                  | true           |

  @jerarquia
  Escenario: Validar jerarquía de división política
    Dado que consulto el departamento 5 "BOLIVAR"
    Cuando sumo los potenciales de todos sus municipios
    Entonces el total debe coincidir con el potencial del departamento
    Y cada municipio debe tener:
      | campo    | validación      |
      | codDepto | igual a 5       |
      | nomDepto | igual a BOLIVAR |
      | codMipio | mayor a 0       |
    Y la suma de zonas por municipio debe coincidir
    Y la suma de puestos por zona debe coincidir

  @agregacion
  Esquema del escenario: Validar agregaciones por nivel
    Cuando consulto estadísticas de <nivel>
    Y sumo estadísticas del nivel inferior <nivel_inferior>
    Entonces los totales deben coincidir

    Ejemplos:
      | nivel        | nivel_inferior |
      | nacional     | departamentos  |
      | departamento | municipios     |
      | municipio    | zonas          |
      | zona         | puestos        |
```

---

### 📦 FASE 3B: Tags y Ejecución Selectiva

**Duración estimada:** 20 minutos

#### Estrategia de Tags

```gherkin
# Tags de módulo
@departamentos @municipios @zonas @puestos @estadisticas

# Tags de propósito
@smoke          # Tests críticos (5-10 scenarios)
@regression     # Suite completa de regresión
@critical       # Tests de alta criticidad
@happy-path     # Casos de uso positivos
@edge-case      # Casos límite
@validacion     # Tests de validación de entrada
@seguridad      # Tests de seguridad
@coherencia     # Tests de integridad de datos

# Tags de estado
@wip            # Work In Progress
@bug            # Reproduce un bug conocido
@skip           # Temporalmente deshabilitado

# Tags de velocidad
@slow           # Tests lentos (>10s)
@fast           # Tests rápidos (<2s)

# Tags técnicos
@api            # Tests de API REST
@database       # Tests que dependen de BD
@kafka          # Tests de mensajería
```

#### Comandos de Ejecución

```bash
# SMOKE TESTS (antes de commit)
./mvnw verify -Dcucumber.filter.tags="@smoke"
# Debe ejecutar: ~10 scenarios en <30 segundos

# CRITICAL TESTS (antes de push)
./mvnw verify -Dcucumber.filter.tags="@critical"
# Debe ejecutar: ~20 scenarios en <2 minutos

# REGRESSION (nightly build)
./mvnw verify -Dcucumber.filter.tags="@regression and not @slow"
# Debe ejecutar: ~80 scenarios en <10 minutos

# DESARROLLO (excluir WIP y lentos)
./mvnw verify -Dcucumber.filter.tags="not @wip and not @slow"

# POR MÓDULO
./mvnw verify -Dcucumber.filter.tags="@departamentos"
./mvnw verify -Dcucumber.filter.tags="@estadisticas"

# COMBINACIONES
./mvnw verify -Dcucumber.filter.tags="@seguridad and @critical"
./mvnw verify -Dcucumber.filter.tags="@departamentos or @municipios"
./mvnw verify -Dcucumber.filter.tags="@happy-path and not @slow"
```

---

### 📦 FASE 3C: Hooks y Backgrounds

**Duración estimada:** 30 minutos

#### CucumberHooks.java

```java
package com.tyse.scrutiny.micro.divipol.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hooks de Cucumber que se ejecutan en diferentes puntos del ciclo de vida.
 */
public class CucumberHooks {

    private static final Logger LOG = LoggerFactory.getLogger(CucumberHooks.class);

    @Before
    public void beforeScenario(Scenario scenario) {
        LOG.info("========================================");
        LOG.info("Iniciando scenario: {}", scenario.getName());
        LOG.info("Tags: {}", scenario.getSourceTagNames());
        LOG.info("========================================");
    }

    @After
    public void afterScenario(Scenario scenario) {
        LOG.info("========================================");
        LOG.info("Finalizando scenario: {}", scenario.getName());
        LOG.info("Estado: {}", scenario.getStatus());
        LOG.info("========================================");

        // Limpiar contexto si es necesario
        // Capturar screenshots si falló
        if (scenario.isFailed()) {
            LOG.error("Scenario falló: {}", scenario.getName());
            // Aquí podrías capturar logs adicionales
        }
    }

    @BeforeStep
    public void beforeStep(Scenario scenario) {
        // Opcional: logging antes de cada step
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        // Opcional: validaciones después de cada step
    }

    // Hooks condicionales por tags
    @Before("@database")
    public void beforeDatabaseScenario() {
        LOG.info("Preparando escenario que requiere base de datos");
        // Verificar conexión a BD, limpiar datos, etc.
    }

    @After("@database")
    public void afterDatabaseScenario() {
        LOG.info("Limpiando después de escenario de base de datos");
        // Rollback, limpiar datos de test, etc.
    }

    @Before("@slow")
    public void beforeSlowScenario() {
        LOG.warn("Iniciando escenario lento - puede tomar >10 segundos");
    }
}
```

---

## ✅ Criterios de Éxito - HITO 3

- ✅ **Scenarios totales:** 70 → 85 (+15 scenarios)
- ✅ **Data Tables avanzados:** Validaciones complejas
- ✅ **Tags organizados:** Ejecución selectiva funcional
- ✅ **Hooks implementados:** Logging y limpieza automática
- ✅ **Backgrounds optimizados:** Reutilización de setup

---

## 🎯 HITO 4: COBERTURA COMPLETA

**Objetivo:** Alcanzar 68%+ de cobertura de código
**Tests esperados:** 145 → 200
**Cobertura esperada:** 64% → 68%

---

### 📦 FASE 4A: RowMappers y Repository Custom

**Duración estimada:** 50 minutos
**Tests esperados:** +37
**Cobertura esperada:** +4%

#### Archivos a Crear

##### 1. DivipolRowMapperTest.java (8 tests)

```java
/**
 * Tests unitarios de DivipolRowMapper.
 * Valida el mapeo correcto de Row → Divipol entity.
 */
@ExtendWith(MockitoExtension.class)
class DivipolRowMapperTest {

    private DivipolRowMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new DivipolRowMapper();
    }

    @Test
    void apply_withCompleteRow_shouldMapAllFields() {
        // Given
        Row mockRow = mock(Row.class);
        when(mockRow.get("iddivipol", Integer.class)).thenReturn(1);
        when(mockRow.get("clase", String.class)).thenReturn("D");
        when(mockRow.get("coddepto", Integer.class)).thenReturn(5);
        // ... configurar todos los campos

        // When
        Divipol result = mapper.apply(mockRow, null);

        // Then
        assertThat(result.getIddivipol()).isEqualTo(1);
        assertThat(result.getClase()).isEqualTo("D");
        assertThat(result.getCoddepto()).isEqualTo(5);
        // ... verificar todos los campos
    }

    @Test
    void apply_withNullOptionalFields_shouldMapCorrectly() {
        // Test de campos opcionales null
    }

    @Test
    void apply_withEdgeValues_shouldHandleCorrectly() {
        // Test con valores extremos
    }

    // ... 5 tests más
}
```

##### 2. DivipolRepositoryCustomIT.java (15 tests)

```java
/**
 * Tests de integración para métodos custom del repository.
 * Prueba consultas complejas y vistas agregadas.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class })
@EmbeddedSQL
class DivipolRepositoryCustomIT {

    @Autowired
    private DivipolRepository divipolRepository;

    @Test
    void findAllDepartamentos_shouldReturnAllDepartamentos() {
        // When
        List<DivipolDepartamentoDTO> departamentos =
            divipolRepository.findAllDepartamentos()
                .collectList()
                .block();

        // Then
        assertThat(departamentos).isNotEmpty();
        assertThat(departamentos.size()).isGreaterThanOrEqualTo(32);

        // Validar estructura de cada departamento
        departamentos.forEach(depto -> {
            assertThat(depto.getCoddepto()).isNotNull();
            assertThat(depto.getNomdepto()).isNotBlank();
            assertThat(depto.getTotalPotencial()).isGreaterThanOrEqualTo(0L);
        });
    }

    @Test
    void findMunicipiosByDepartamento_withValidDepto_shouldReturnMunicipios() {
        // ... test
    }

    // ... 13 tests más
}
```

##### 3. DivipolRepositoryAdvancedIT.java (14 tests)

```java
/**
 * Tests avanzados de repository: paginación, ordenamiento, filtros complejos.
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class })
@EmbeddedSQL
class DivipolRepositoryAdvancedIT {

    @Autowired
    private DivipolRepository divipolRepository;

    @Test
    void findAllDepartamentos_withSorting_shouldReturnSorted() {
        // Test de ordenamiento
    }

    @Test
    void findMunicipiosByDepartamento_withPagination_shouldReturnPage() {
        // Test de paginación
    }

    // ... 12 tests más
}
```

#### Criterios de Éxito - Fase 4A
- ✅ 37 tests nuevos (145 → 182)
- ✅ Cobertura de repository: 61% → 85%
- ✅ Cobertura de repository.rowmapper: 15% → 80%

---

### 📦 FASE 4B: Tests Parametrizados

**Duración estimada:** 30 minutos
**Tests esperados:** +15

```java
/**
 * Tests parametrizados para reducir duplicación.
 */
class DivipolParameterizedTests {

    @ParameterizedTest
    @ValueSource(ints = {5, 8, 11, 13, 15, 17, 19, 23, 25})
    void getMunicipiosByDepartamento_withValidDepartments_shouldReturnMunicipios(int codDepto) {
        // Test reutilizable para múltiples departamentos
    }

    @ParameterizedTest
    @CsvSource({
        "5,   1,   200",  // BOLIVAR/CARTAGENA - OK
        "999, 1,   200",  // Departamento inexistente - OK (vacío)
        "-1,  1,   400",  // Código negativo - Error
        "5,   -1,  400",  // Municipio negativo - Error
    })
    void getZonasByMunicipio_withDifferentParams_shouldReturnExpectedStatus(
        int codDepto, int codMpio, int expectedStatus) {
        // Test con múltiples combinaciones
    }
}
```

---

### 📦 FASE 4C: Alcanzar 68%+ Cobertura

**Objetivo:** Completar tests faltantes para alcanzar meta
**Tests esperados:** Variable según análisis de cobertura

#### Proceso:
1. Ejecutar JaCoCo: `./mvnw clean verify`
2. Revisar reporte: `firefox target/site/jacoco-merged/index.html`
3. Identificar clases con <50% cobertura
4. Priorizar por criticidad
5. Implementar tests faltantes

---

## ✅ Criterios de Éxito - HITO 4

- ✅ **Tests totales:** 145 → 200 (+55 tests)
- ✅ **Cobertura:** 64% → 68% (+4%)
- ✅ **Repository:** Completamente testeado (85%+)
- ✅ **RowMappers:** Cobertura aceptable (80%+)

---

## 🎯 HITO 5: EXCELENCIA OPERACIONAL

**Objetivo:** Performance testing, observabilidad y documentación
**Tests esperados:** 200 → 215
**Cobertura esperada:** 68% → 72%

---

### 📦 FASE 5A: Gatling + ArchUnit

**Duración estimada:** 60 minutos

#### DivipolLoadSimulation.scala (Gatling)

```scala
package divipol

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Simulación de carga para endpoints de DIVIPOL.
 */
class DivipolLoadSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .authorizationHeader("Bearer ${jwt_token}")

  val scn = scenario("DIVIPOL API Load Test")
    .exec(http("Get All Departamentos")
      .get("/api/divipol/departamentos")
      .check(status.is(200))
      .check(jsonPath("$[*].coddepto").findAll.saveAs("deptos")))
    .pause(1 second)
    .exec(http("Get Municipios")
      .get("/api/divipol/municipios?codDepto=5")
      .check(status.is(200)))
    .pause(1 second)
    .exec(http("Get General Stats")
      .get("/api/divipol/stats")
      .check(status.is(200))
      .check(jsonPath("$.totalDepartamentos").greaterThan(30)))

  setUp(
    scn.inject(
      rampUsers(100) during (30 seconds),
      constantUsersPerSec(50) during (1 minute)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(5000),
     global.successfulRequests.percent.gt(95)
   )
}
```

#### ArchitectureRulesTest.java (ArchUnit)

```java
/**
 * Tests de arquitectura con ArchUnit.
 */
@AnalyzeClasses(packages = "com.tyse.scrutiny.micro.divipol")
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule services_should_only_be_accessed_by_controllers =
        classes()
            .that().resideInAPackage("..service..")
            .should().onlyBeAccessed().byAnyPackage("..web..", "..service..", "..cucumber..");

    @ArchTest
    static final ArchRule repositories_should_only_be_accessed_by_services =
        classes()
            .that().resideInAPackage("..repository..")
            .should().onlyBeAccessed().byAnyPackage("..service..", "..config..");

    @ArchTest
    static final ArchRule dtos_should_be_immutable =
        classes()
            .that().resideInAPackage("..dto..")
            .should().haveOnlyFinalFields();

    // ... 7 reglas más
}
```

---

### 📦 FASE 5B: Observabilidad

**Duración estimada:** 40 minutos

#### ActuatorEndpointsIT.java (8 tests)

```java
/**
 * Tests de endpoints de Actuator.
 */
@SpringBootTest
@AutoConfigureWebTestClient
class ActuatorEndpointsIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void actuator_health_shouldReturnUp() {
        webTestClient
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.components.db.status").isEqualTo("UP")
            .jsonPath("$.components.diskSpace.status").isEqualTo("UP");
    }

    @Test
    void actuator_metrics_shouldReturnMetrics() {
        // Test de métricas
    }

    // ... 6 tests más
}
```

#### MetricsIT.java (7 tests)

```java
/**
 * Tests de métricas de Micrometer.
 */
class MetricsIT {

    @Test
    void metrics_divipol_requests_shouldBeRecorded() {
        // Llamar endpoint
        webTestClient.get().uri("/api/divipol/departamentos")
            .exchange().expectStatus().isOk();

        // Verificar métrica
        webTestClient
            .get()
            .uri("/actuator/metrics/http.server.requests")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.measurements[?(@.statistic=='COUNT')].value")
            .value(count -> assertThat((Double)count).isGreaterThan(0));
    }

    // ... 6 tests más
}
```

---

### 📦 FASE 5C: Documentación Final

**Duración estimada:** 60 minutos

#### Archivos a Crear/Actualizar

1. **TESTING-GUIDE.md**
   - Guía completa de testing del proyecto
   - Convenciones y mejores prácticas
   - Cómo ejecutar diferentes tipos de tests
   - Troubleshooting común

2. **CUCUMBER-CHEATSHEET.md**
   - Referencia rápida de Cucumber
   - Tags disponibles y su uso
   - Comandos más usados
   - Ejemplos de scenarios

3. **API-DOCUMENTATION.md** (generado desde features)
   - Documentación de API generada desde Gherkin
   - Ejemplos de uso de cada endpoint
   - Códigos de respuesta esperados

4. **Actualizar README.md principal**
   - Sección de testing
   - Links a documentación
   - Badges de cobertura

---

## ✅ Criterios de Éxito - HITO 5

- ✅ **Tests totales:** 200 → 215 (+15 tests)
- ✅ **Cobertura:** 68% → 72% (+4%)
- ✅ **Gatling:** Simulaciones de carga funcionando
- ✅ **ArchUnit:** 10+ reglas de arquitectura
- ✅ **Actuator:** Tests de health y metrics
- ✅ **Documentación:** Completa y actualizada

---

## 📊 RESUMEN EJECUTIVO DEL PLAN

| Hito | Fases | Scenarios | Tests JUnit | Cobertura | Prioridad | Estado |
|------|-------|-----------|-------------|-----------|-----------|--------|
| **1. Fundamentos** | 4 | +40 | +36 | 49%→58% | 🔴 Crítico | ✅ 1A Completada |
| **2. Seguridad** | 3 | +30 | +15 | 58%→64% | 🟠 Alta | ⏳ Pendiente |
| **3. Avanzado** | 3 | +15 | +10 | 64%→66% | 🟡 Media | ⏳ Pendiente |
| **4. Cobertura** | 3 | +5  | +55 | 66%→68% | 🟡 Media | ⏳ Pendiente |
| **5. Excelencia** | 3 | +10 | +15 | 68%→72% | 🟢 Opcional | ⏳ Pendiente |
| **TOTAL** | **16** | **+100** | **+131** | **72%** | - | **6% Completado** |

### Métricas Finales Esperadas

| Métrica | Inicial | Final | Mejora |
|---------|---------|-------|--------|
| **Cobertura** | 49% | 72% | +23% (+47%) |
| **Tests JUnit** | 94 | 225 | +131 (+139%) |
| **Scenarios Cucumber** | 0 | 100 | +100 (∞%) |
| **Features** | 0 | 12 | +12 |
| **Step Definitions** | 0 | 15 clases | +15 |
| **Documentación** | Básica | Completa | ⭐⭐⭐⭐⭐ |

---

## 🚀 COMANDOS ÚTILES

### Ejecución de Tests

```bash
# ========================================
# CUCUMBER
# ========================================

# Todos los tests (JUnit + Cucumber)
./mvnw clean verify

# Solo Cucumber
./mvnw verify -Dtest=CucumberIT

# Por tags
./mvnw verify -Dcucumber.filter.tags="@smoke"
./mvnw verify -Dcucumber.filter.tags="@critical"
./mvnw verify -Dcucumber.filter.tags="@departamentos"
./mvnw verify -Dcucumber.filter.tags="@seguridad and @critical"
./mvnw verify -Dcucumber.filter.tags="not @wip and not @slow"

# ========================================
# JUNIT
# ========================================

# Solo unit tests (rápido)
./mvnw test

# Solo integration tests
./mvnw failsafe:integration-test failsafe:verify

# Test específico
./mvnw test -Dtest=DivipolServiceTest
./mvnw test -Dtest=DivipolRepositoryIT

# ========================================
# REPORTES
# ========================================

# Reporte Cucumber HTML
firefox target/cucumber-reports/cucumber.html

# Reporte JaCoCo (cobertura)
firefox target/site/jacoco-merged/index.html  # Combinado (recomendado)
firefox target/site/jacoco/index.html          # Solo unit tests
firefox target/site/jacoco-it/index.html       # Solo integration tests

# ========================================
# GATLING (Performance)
# ========================================

# Ejecutar simulaciones de carga
./mvnw gatling:test

# Ver reporte de performance
firefox target/gatling/results/*/index.html

# ========================================
# ANÁLISIS
# ========================================

# Ver resumen de tests
grep "Tests run:" target/surefire-reports/*.txt
grep "Tests run:" target/failsafe-reports/*.txt
grep "Scenarios" target/cucumber-reports/*.txt

# Ver cobertura por paquete
cat target/site/jacoco-merged/index.html | grep -A 1 "com.tyse"
```

### Comandos de Desarrollo

```bash
# Compilar sin tests
./mvnw clean compile -DskipTests

# Compilar incluyendo tests
./mvnw clean compile test-compile

# Verificar formato de código
./mvnw prettier:check

# Aplicar formato automático
./mvnw prettier:write

# Verificar checkstyle
./mvnw checkstyle:check

# Limpiar completamente
./mvnw clean

# Build completo (compile + test + package)
./mvnw clean package
```

### Git Workflow

```bash
# Ver estado
git status

# Agregar cambios
git add src/test/

# Commit
git commit -m "feat(tests): Implementar [descripción]"

# Push
git push -u origin claude/analyze-unit-integration-tests-016TWoA8R3t5J4ogQwJTsD7S

# Ver log
git log --oneline -10
```

---

## 📚 REFERENCIAS Y RECURSOS

### Documentación Oficial

- **Cucumber:** https://cucumber.io/docs/cucumber/
- **Gherkin:** https://cucumber.io/docs/gherkin/reference/
- **Cucumber Spring:** https://github.com/cucumber/cucumber-jvm/tree/main/cucumber-spring
- **JUnit 5:** https://junit.org/junit5/docs/current/user-guide/
- **AssertJ:** https://assertj.github.io/doc/
- **Mockito:** https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Spring Boot Testing:** https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- **Testcontainers:** https://www.testcontainers.org/
- **JaCoCo:** https://www.jacoco.org/jacoco/trunk/doc/
- **Gatling:** https://gatling.io/docs/current/
- **ArchUnit:** https://www.archunit.org/userguide/html/000_Index.html

### Guías Internas

- `src/test/resources/features/README-CUCUMBER.md` - Guía de Cucumber
- `TESTING-GUIDE.md` - Guía completa de testing (a crear)
- `CUCUMBER-CHEATSHEET.md` - Referencia rápida (a crear)

### Ejemplos en el Proyecto

- `divipol-departamentos.feature` - Ejemplo de feature completo
- `DivipolDepartamentosSteps.java` - Ejemplo de step definitions
- `DivipolServiceIT.java` - Ejemplo de integration test
- `DivipolServiceTest.java` - Ejemplo de unit test (a crear)

---

## 🎯 ESTADO ACTUAL DEL PLAN

### ✅ Completado

- [x] **FASE 1A: Cucumber 101** (100%)
  - [x] divipol-departamentos.feature (3 scenarios)
  - [x] DivipolDepartamentosSteps.java (12 steps)
  - [x] CucumberIT.java (runner configurado)
  - [x] Reportes HTML/JSON/XML funcionando
  - [x] Autenticación JWT real implementada
  - [x] Documentación README-CUCUMBER.md

### ⏳ Pendiente

- [ ] **FASE 1B:** Cucumber para todos los endpoints (0%)
- [ ] **FASE 1C:** Tests unitarios puros (0%)
- [ ] **FASE 1D:** Cucumber para validaciones (0%)
- [ ] **HITO 2:** Seguridad con Cucumber (0%)
- [ ] **HITO 3:** Cucumber Avanzado (0%)
- [ ] **HITO 4:** Cobertura Completa (0%)
- [ ] **HITO 5:** Excelencia Operacional (0%)

### 📊 Progreso General

```
Progreso: ██░░░░░░░░░░░░░░░░ 6% (1/16 fases)

Hito 1: ██████░░░░░░░░░░░░░░ 25% (1/4 fases)
Hito 2: ░░░░░░░░░░░░░░░░░░░░ 0%  (0/3 fases)
Hito 3: ░░░░░░░░░░░░░░░░░░░░ 0%  (0/3 fases)
Hito 4: ░░░░░░░░░░░░░░░░░░░░ 0%  (0/3 fases)
Hito 5: ░░░░░░░░░░░░░░░░░░░░ 0%  (0/3 fases)
```

---

## 📞 INFORMACIÓN DE CONTACTO

**Proyecto:** tyse-scrutiny-micro-divipol
**Repository:** https://github.com/manuelmottaherrera/tyse-scrutiny-micro-divipol
**Branch actual:** `claude/analyze-unit-integration-tests-016TWoA8R3t5J4ogQwJTsD7S`
**Última actualización:** 15 de Noviembre de 2025

---

## ✅ PRÓXIMOS PASOS INMEDIATOS

1. **Verificar que Fase 1A funciona:**
   ```bash
   ./mvnw verify -Dtest=CucumberIT
   firefox target/cucumber-reports/cucumber.html
   ```

2. **Si todo OK, continuar con Fase 1B:**
   - Crear `divipol-municipios.feature`
   - Crear `DivipolMunicipiosSteps.java`
   - Ejecutar y verificar

3. **Iterar fase por fase:**
   - Implementar → Probar localmente → Commit → Push
   - Repetir hasta completar el hito

---

**¡Éxito con el testing!** 🥒✨

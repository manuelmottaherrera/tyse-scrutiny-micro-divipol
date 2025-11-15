# 🥒 Guía de Cucumber para DIVIPOL

## 📚 ¿Qué es Cucumber?

Cucumber es un framework de **BDD (Behavior-Driven Development)** que permite escribir tests en **lenguaje natural** usando la sintaxis **Gherkin**.

### Beneficios:

- ✅ Tests legibles por **cualquier persona** (técnicos y no técnicos)
- ✅ Documentación **viva** del comportamiento de la API
- ✅ Comunicación clara entre **negocio, desarrollo y QA**
- ✅ Tests que sirven como **especificación**

---

## 🏗️ Estructura de Cucumber en este Proyecto

```
src/test/
├── java/.../cucumber/
│   ├── CucumberIT.java                    # Runner principal
│   ├── CucumberTestContextConfiguration.java  # Configuración Spring
│   └── stepdefs/
│       ├── StepDefs.java                  # Clase base
│       └── DivipolDepartamentosSteps.java # Step definitions
│
└── resources/
    └── features/
        ├── README-CUCUMBER.md              # Esta guía
        └── divipol-departamentos.feature   # Features en Gherkin
```

---

## 📝 Sintaxis Gherkin

### Estructura básica de un Feature:

```gherkin
# language: es
@tag1 @tag2
Característica: Título descriptivo
  Descripción opcional que explica el contexto
  de esta funcionalidad

  Antecedentes:
    # Steps que se ejecutan ANTES de cada escenario
    Dado que el sistema está inicializado

  @tag-escenario
  Escenario: Descripción del comportamiento
    Dado [contexto inicial]
    Cuando [acción que se ejecuta]
    Entonces [resultado esperado]
    Y [validación adicional]
```

### Palabras clave en español:

- **Característica:** Define el feature completo
- **Antecedentes:** Steps que se ejecutan antes de cada escenario
- **Escenario:** Un caso de prueba específico
- **Dado / Dada:** Precondiciones (GIVEN)
- **Cuando:** Acción que se ejecuta (WHEN)
- **Entonces:** Resultado esperado (THEN)
- **Y:** Continuar el step anterior
- **Pero:** Negación o excepción

### Esquema del escenario (Data-Driven):

```gherkin
Esquema del escenario: Validar códigos de departamento
  Cuando consulto municipios con código <codigo>
  Entonces recibo un código de respuesta <status>

  Ejemplos:
    | codigo | status |
    | 5      | 200    |
    | 999    | 404    |
    | -1     | 400    |
```

---

## 🎯 Tags (Etiquetas)

Los tags permiten categorizar y ejecutar escenarios selectivamente.

### Tags usados en este proyecto:

- `@departamentos` - Tests de departamentos
- `@municipios` - Tests de municipios
- `@zonas` - Tests de zonas
- `@puestos` - Tests de puestos
- `@estadisticas` - Tests de estadísticas

### Tags de propósito:

- `@smoke` - Tests críticos de humo
- `@regression` - Tests de regresión
- `@critical` - Tests críticos
- `@happy-path` - Casos felices
- `@seguridad` - Tests de seguridad
- `@validacion` - Tests de validación

### Tags de estado:

- `@wip` - Work In Progress (en desarrollo)
- `@bug` - Tests que reproducen bugs
- `@skip` - Tests a ignorar temporalmente

---

## 🚀 Comandos de Ejecución

### Ejecutar TODOS los tests (JUnit + Cucumber):

```bash
./mvnw clean verify
```

### Ejecutar SOLO Cucumber:

```bash
./mvnw verify -Dtest=CucumberIT
```

### Ejecutar por tags:

```bash
# Solo smoke tests
./mvnw verify -Dcucumber.filter.tags="@smoke"

# Solo tests de departamentos
./mvnw verify -Dcucumber.filter.tags="@departamentos"

# Solo tests críticos
./mvnw verify -Dcucumber.filter.tags="@critical"

# Excluir WIP
./mvnw verify -Dcucumber.filter.tags="not @wip"

# Combinación AND
./mvnw verify -Dcucumber.filter.tags="@departamentos and @smoke"

# Combinación OR
./mvnw verify -Dcucumber.filter.tags="@departamentos or @municipios"
```

---

## 📊 Reportes

### Reporte HTML:

```bash
# Después de ejecutar los tests
firefox target/cucumber-reports/cucumber.html
```

El reporte HTML incluye:
- ✅ Scenarios passed/failed
- ⏱️ Tiempo de ejecución
- 📊 Estadísticas por feature
- 📝 Steps ejecutados
- 🎯 Tags usados

### Reporte JSON (para CI/CD):

```bash
cat target/cucumber-reports/cucumber.json
```

### Reporte XML (para Jenkins):

```bash
cat target/cucumber-reports/cucumber.xml
```

---

## 💡 Ejemplo Completo

### 1. Feature (divipol-departamentos.feature):

```gherkin
# language: es
@departamentos @smoke
Característica: Consultar departamentos
  Como usuario de la API
  Quiero consultar departamentos
  Para obtener información de división política

  Antecedentes:
    Dado que la base de datos tiene datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Consultar todos los departamentos
    Cuando consulto el endpoint GET "/api/divipol/departamentos"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene al menos 32 departamentos
```

### 2. Step Definitions (DivipolDepartamentosSteps.java):

```java
@Dado("que la base de datos tiene datos de DIVIPOL")
public void queBaseDeDatosTieneDatos() {
    // Setup
}

@Cuando("consulto el endpoint GET {string}")
public void consultoEndpoint(String endpoint) {
    actions = webTestClient.get().uri(endpoint).exchange();
}

@Entonces("recibo un código de respuesta {int}")
public void reciboCodigoRespuesta(int statusCode) {
    actions.expectStatus().isEqualTo(statusCode);
}
```

---

## 🎓 Buenas Prácticas

### 1. **Escribir en lenguaje de negocio:**

❌ **Mal:**
```gherkin
Cuando hago POST a /api/divipol con header Authorization
```

✅ **Bien:**
```gherkin
Cuando consulto los departamentos como usuario autenticado
```

### 2. **Steps reutilizables:**

Diseña steps genéricos que puedas reutilizar:

```gherkin
Cuando consulto el endpoint GET "/api/divipol/departamentos"
Cuando consulto el endpoint GET "/api/divipol/municipios"
# Mismo step, diferentes endpoints
```

### 3. **Usar DataTables para datos complejos:**

```gherkin
Y cada departamento tiene los siguientes campos:
  | campo          |
  | coddepto       |
  | nomdepto       |
  | totalPotencial |
```

### 4. **Un escenario = una funcionalidad:**

Cada escenario debe probar **una cosa específica**.

### 5. **Tags organizados:**

```gherkin
@departamentos @smoke @critical
Escenario: Consultar departamentos (smoke test)

@departamentos @validacion
Escenario: Validar códigos inválidos
```

---

## 🔧 Troubleshooting

### Problema: "Undefined step"

**Error:**
```
Step [consulto el endpoint] is undefined
```

**Solución:**
Implementar el step definition en DivipolDepartamentosSteps.java

### Problema: "No features found"

**Error:**
```
No features found at [classpath:features]
```

**Solución:**
- Verificar que los .feature estén en `src/test/resources/features/`
- Verificar que CucumberIT.java tenga `@SelectClasspathResource("features")`

### Problema: Tests no se ejecutan

**Solución:**
```bash
# Limpiar y recompilar
./mvnw clean compile test-compile

# Ejecutar con log detallado
./mvnw verify -X
```

---

## 📖 Recursos Adicionales

- [Cucumber Docs](https://cucumber.io/docs/cucumber/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [Cucumber Spring](https://github.com/cucumber/cucumber-jvm/tree/main/cucumber-spring)
- [AssertJ](https://assertj.github.io/doc/)

---

## ✅ Checklist de Nuevo Feature

Cuando crees un nuevo feature:

- [ ] Crear archivo `.feature` en `src/test/resources/features/`
- [ ] Usar `# language: es` al inicio
- [ ] Agregar tags apropiados
- [ ] Escribir escenarios en lenguaje de negocio
- [ ] Crear step definitions en `cucumber/stepdefs/`
- [ ] Extender `StepDefs` para compartir estado
- [ ] Usar `@Autowired` para inyectar beans
- [ ] Ejecutar tests: `./mvnw verify`
- [ ] Revisar reporte HTML
- [ ] Documentar steps complejos

---

**¡Feliz testing con Cucumber!** 🥒✨

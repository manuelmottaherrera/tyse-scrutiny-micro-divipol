# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## RULES

Responder en español.

## Project Overview

Microservicio **JHipster 8.11.0** llamado **tyseScrutinyMicroDivipol** que gestiona datos de división política (divipol) de Colombia y el sistema de testigos electorales para el sistema Tyse Scrutiny.

**Stack:** Spring Boot 3.4.5 + Spring WebFlux (reactivo) + PostgreSQL/R2DBC + Consul + Kafka + JWT + OpenAPI code generation + Liquibase.

**Puerto:** 8081. Requiere Consul en `localhost:8500` para arrancar.

## Development Commands

### Levantar servicios

```bash
# 1. Infraestructura compartida (Consul, Kafka, MinIO, MailHog) - UNA SOLA VEZ
cd ../tyse-scrutiny-infrastructure && docker compose up -d

# 2. PostgreSQL de este microservicio (puerto 5433)
docker compose -f src/main/docker/postgresql.yml up --wait

# 3. Arrancar la aplicación
./mvnw
```

### Testing

```bash
# Todos los tests (unit + integration + Cucumber BDD)
./mvnw verify

# Solo unit tests
./mvnw test

# Un solo unit test
./mvnw test -Dtest=DivipolTest

# Solo integration tests (clases *IT y *IntTest, usan Testcontainers)
./mvnw failsafe:integration-test

# Un solo integration test
./mvnw failsafe:integration-test -Dit.test=DivipolSearchServiceIT

# Cucumber BDD por tag
./mvnw verify -Dcucumber.filter.tags="@testigos"
./mvnw verify -Dcucumber.filter.tags="@smoke"
./mvnw verify -Dcucumber.filter.tags="not @wip"

# Performance (Gatling)
./mvnw gatling:test
```

### CI y Push

```bash
# Pipeline CI local completo (3-5 min): tests + quality checks + reportes
./scripts/ci-local.sh
./scripts/ci-local.sh --with-performance    # incluye Gatling (5-7 min)

# CI + push automático (solo pushea si CI pasa)
./scripts/push.sh
./scripts/push.sh origin develop
./scripts/push.sh --skip-ci                 # solo emergencias
```

### Build y Docker

```bash
./mvnw -Pprod clean verify                  # JAR producción
npm run java:docker                         # imagen Docker (amd64)
npm run java:docker:arm64                   # imagen Docker (ARM64)
```

### Code Quality

```bash
npm run prettier:format                     # formatear código
npm run prettier:check                      # verificar formato
npm run backend:nohttp:test                 # checkstyle + nohttp
```

### OpenAPI Code Generation

```bash
# Después de modificar src/main/resources/swagger/api.yml:
./mvnw generate-sources
```

Genera interfaces en `web.api/` y DTOs en `service/api/dto/`. Implementar delegates con `@Service`.

## Architecture

### Microservicio dentro de ecosistema mayor

```
tyse-scrutiny-infrastructure/ (levantar UNA vez):
├── Consul (localhost:8500)         ← Service discovery
├── Kafka (localhost:9092)          ← Message broker
├── MinIO (localhost:9000/9001)     ← Object storage
└── MailHog (localhost:1025/8025)   ← SMTP desarrollo

Gateway (tysescrutinygateway) → Puerto 8080, PostgreSQL :5432
Microservicio Divipol (este repo) → Puerto 8081, PostgreSQL :5433
Microservicio Scrutiny (en desarrollo) → Puerto 8084, PostgreSQL :5434
```

Comunicación entre servicios vía Consul (discovery) y Kafka (mensajería, topic: `sse-topic`).

### Estructura de paquetes (com.tyse.scrutiny.micro.divipol)

- **`domain/`** - Entidades R2DBC: `Divipol`, `MesaVotacion`, `TestigoElectoral`, `Credencial`, `Reclamacion`, `ComisionEscrutadora`, `OrganizacionPolitica`, `ConfiguracionElectoral`, etc.
- **`repository/`** - Repositorios R2DBC. `DivipolRepositoryCustomImpl` tiene queries complejos con `DatabaseClient` (búsqueda full-text, paginación).
- **`service/`** - Lógica de negocio. Sub-paquetes `export/` (PDF con OpenPDF, CSV) y `util/` (validación).
- **`web/rest/`** - Controllers REST (delegates generados desde OpenAPI).
- **`web/api/`** - Interfaces generadas por OpenAPI codegen. **No editar manualmente.**
- **`broker/`** - Kafka producers/consumers.
- **`config/`** - Configuración Spring (Security, Database, Liquibase, OpenAPI).

### Patrones clave

**Todo es reactivo:** Controllers retornan `Mono<T>` o `Flux<T>`. Repositorios extienden `R2dbcRepository`. Nunca usar `block()` — mantener cadena reactiva end-to-end.

**API-first:** Editar `swagger/api.yml` → `./mvnw generate-sources` → implementar delegates con `@Service`.

**Custom repository pattern:** Interface `DivipolRepositoryCustom` + implementación `DivipolRepositoryCustomImpl` usando `DatabaseClient` para SQL reactivo con queries complejos.

**PostgreSQL POINT type:** La columna `cordenadas` de `divipol` usa tipo POINT nativo. Los changesets Liquibase usan tabla auxiliar para convertir strings a POINT durante carga CSV.

### Perfiles Spring

| Perfil | Uso | Servicios |
|--------|-----|-----------|
| `dev` (default) | Desarrollo local | localhost (Consul :8500, DB :5433, Kafka :9092) |
| `local-dev` | Servidor remoto de desarrollo | Apunta a 192.168.0.58 (Consul :8510, DB :5433, Kafka :9102) |
| `docker-dev` | Dentro de Docker Compose | Hostnames Docker (consul, postgres-divipol, kafka) |
| `prod` | Producción | Optimizado, logging mínimo |
| `no-liquibase` | Deshabilitar migraciones | Combinar con otros perfiles |

```bash
./mvnw -Dspring.profiles.active=local-dev
```

## Database Schema

### Divipol (datos geográficos)

18,016 registros de puestos, zonas, municipios y departamentos. Campo `clase`: N=Nacional, D=Departamento, M=Municipio, P=Puesto. Códigos jerárquicos: `coddepto`, `codmipio`, `codzona`, `codpuesto`.

4 vistas agregadas: `view_divipol_departamento`, `view_divipol_municipio`, `view_divipol_zona`, `view_divipol_puesto`.

### Sistema de testigos electorales

Implementa normativa colombiana (Ley 1475/2011, Código Electoral, Resolución CNE 09458/2025).

```
organizacion_politica → testigo_electoral → testigo_mesa → mesa_votacion → divipol
                                          → testigo_comision → comision_escrutadora
credencial → testigo_mesa | testigo_comision
reclamacion → testigo_electoral + mesa_votacion | comision_escrutadora
configuracion_electoral (parámetros de plazos y límites)
```

**Reglas normativas:**
1. Máximo 1 testigo principal por organización por mesa
2. Remanentes: <10 mesas = máx 1 por org; ≥10 mesas = hasta 10% del total
3. Período de inscripción validado contra `configuracion_electoral`
4. Credenciales (E15/E16) solo con asignación activa

**Migraciones Liquibase:** `src/main/resources/config/liquibase/`. Master: `master.xml`. En dev ejecuta contextos `dev, faker`.

## Testing

### Infraestructura de tests

- **`@IntegrationTest`** - Anotación compuesta: SpringBootTest + Testcontainers (PostgreSQL) + EmbeddedKafka
- **`@AutoConfigureWebTestClient(timeout = "PT10S")`** - Para tests REST reactivos
- **`@WithMockUser`** - Autenticación mock para tests
- **BlockHound** detecta llamadas bloqueantes en código reactivo
- **ArchUnit** valida reglas arquitectónicas

### Cucumber BDD

Features en español en `src/test/resources/features/`. Step definitions en `src/test/java/.../cucumber/stepdefs/`. Tags disponibles: `@testigos`, `@organizaciones`, `@comisiones`, `@reclamaciones`, `@departamentos`, `@smoke`, `@wip`.

## Git Workflow

**Branches protegidos:** `main` (producción) y `develop` (integración). No push directo — solo PR con aprobación de `@manuelmottaherrera`.

**Convención de branches:** `feature/<nombre>-<descripcion>`, `hotfix/<descripcion>`.

**Convención de commits:**
```
type(scope): descripción breve

Tipos: feat | fix | docs | style | refactor | test | chore
```

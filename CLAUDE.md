# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## RULES

Responder en español

## Project Overview

This is a **JHipster 8.11.0** microservice application called **tyseScrutinyMicroDivipol** that provides political division (divipol) data services for the Tyse Scrutiny system. It is built with:

- **Spring Boot 3.4.5** with **Spring WebFlux** (reactive stack)
- **PostgreSQL** with **R2DBC** for reactive database access
- **Consul** for service discovery and configuration (required at http://localhost:8500)
- **Kafka** for message streaming
- **JWT authentication**
- **OpenAPI/Swagger** for API-first development
- **Liquibase** for database migrations

The application runs on **port 8081** and is designed to be part of a microservice architecture.

## Development Commands

### Running the Application

```bash
# Start in development mode (default profile)
./mvnw

# Start with specific Spring profile
./mvnw -Dspring.profiles.active=dev

# Start with remote debugging on port 8000
npm run backend:debug
```

**Important**: The application requires Consul at http://localhost:8500 to be running. It will refuse to start without it.

### Database

```bash
# Start PostgreSQL (runs on port 5433)
docker compose -f src/main/docker/postgresql.yml up --wait

# Stop PostgreSQL
docker compose -f src/main/docker/postgresql.yml down -v

# Database connection details (dev):
# - URL: r2dbc:postgresql://localhost:5433/tyseScrutinyMicroDivipol
# - Username: tyseScrutinyMicroDivipol
# - Password: (empty)
```

### Required Services

**IMPORTANTE**: Este es un microservicio dentro de una arquitectura más grande. Los servicios compartidos (Kafka, Consul) deben levantarse **UNA SOLA VEZ** desde el gateway o desde un docker-compose central, NO desde cada microservicio.

```bash
# Solo levantar PostgreSQL del microservicio (port 5433)
docker compose -f src/main/docker/postgresql.yml up --wait

# Kafka y Consul deben estar corriendo desde el gateway
# Verificar que estén corriendo:
docker ps | grep -E "kafka|consul"

# Si necesitas levantarlos manualmente (desde el gateway o central):
# docker compose -f src/main/docker/consul.yml up --wait
# docker compose -f src/main/docker/kafka.yml up --wait  # ¡NO ejecutar desde aquí!
```

**Configuración de Kafka**: Este microservicio está configurado para conectarse a `localhost:9092` (el Kafka compartido del gateway).

### Testing

```bash
# Run all unit tests
./mvnw verify

# Run unit tests without logs
npm run backend:unit:test

# Run integration tests (includes *IT and *IntTest classes)
./mvnw failsafe:integration-test

# Run Cucumber BDD tests
./mvnw integration-test

# Run Gatling performance tests
./mvnw gatling:test
```

### Building

```bash
# Build for production (creates optimized JAR)
./mvnw -Pprod clean verify

# Build JAR (production)
npm run java:jar:prod

# Build WAR
./mvnw -Pprod,war clean verify

# Build Docker image
npm run java:docker

# Build Docker image for ARM64 (Apple Silicon)
npm run java:docker:arm64
```

### Code Quality

```bash
# Format code with Prettier
npm run prettier:format

# Check code formatting
npm run prettier:check

# Run checkstyle (including nohttp checks)
npm run backend:nohttp:test

# Start SonarQube locally
docker compose -f src/main/docker/sonar.yml up -d

# Run Sonar analysis
./mvnw -Pprod clean verify sonar:sonar -Dsonar.login=admin -Dsonar.password=admin
```

### OpenAPI Code Generation

```bash
# Generate API code from swagger/api.yml
./mvnw generate-sources

# Edit OpenAPI spec with Swagger Editor
docker compose -f src/main/docker/swagger-editor.yml up -d
# Then open http://localhost:7742
```

After generating sources, implement the delegate classes with `@Service` annotation.

## Architecture

### Microservice Architecture

Este microservicio es parte de una arquitectura más grande con el gateway **tyseScrutinyGateway**:

```
Servicios Compartidos (levantar UNA vez):
├── Kafka (localhost:9092)          ← Compartido entre todos
├── Consul (localhost:8500)         ← Compartido entre todos
└── JHipster Registry (opcional)

Gateway (tysescrutinygateway):
├── Puerto: 8080
└── PostgreSQL: localhost:5432

Microservicio Divipol (este repo):
├── Puerto: 8081
└── PostgreSQL: localhost:5433
```

**Comunicación entre servicios:**

- Gateway ↔ Microservicio: Vía Consul (service discovery)
- Gateway ↔ Microservicio: Mensajería vía Kafka (topics compartidos)
- Microservicio → Kafka: Topic `sse-topic` (consumer)
- Microservicio → Kafka: Producer configurado

**Topics Kafka configurados:**

- `sse-topic` - Consumer del microservicio (grupo: `tyse-scrutiny-micro-divipol`)

### Package Structure

- `com.tyse.scrutiny.micro.divipol.TyseScrutinyMicroDivipolApp` - Main application class
- `aop/` - Aspect-oriented programming (logging aspects)
- `broker/` - Kafka producers and consumers
- `config/` - Spring configuration classes (security, database, etc.)
- `domain/` - Entity classes (JPA/R2DBC entities)
- `management/` - Management and health check endpoints
- `repository/` - R2DBC repositories and custom query implementations
- `security/` - Security utilities and JWT handling
- `web/rest/` - REST controllers
- `web/api/` - Generated OpenAPI delegates (generated from swagger/api.yml)
- `service/api/dto/` - Generated DTOs from OpenAPI spec

### Reactive Architecture

This application uses **Spring WebFlux** and **R2DBC** for fully reactive, non-blocking operations:

- Controllers return `Mono<T>` or `Flux<T>`
- Database access uses R2DBC repositories
- HTTP clients use WebClient
- Kafka integration uses Spring Cloud Stream with reactive bindings

### Service Discovery

The application registers itself with **Consul** on startup. Consul must be running at http://localhost:8500 or the application will fail to start. Configuration can also be externalized to Consul.

### API-First Development

This project follows **API-first** development:

1. Define API endpoints in `src/main/resources/swagger/api.yml`
2. Run `./mvnw generate-sources` to generate interfaces
3. Implement generated delegates in service classes with `@Service` annotation
4. The OpenAPI generator creates controller stubs in `web.api` package

### Database Schema - Divipol

El microservicio gestiona datos de **división política de Colombia** (divipol):

**Tabla principal: `divipol`**

- 18,016 registros de puestos, zonas, municipios y departamentos
- Contiene información electoral: potencial de votantes, número de mesas
- Campo especial: `cordenadas` (tipo POINT de PostgreSQL) para geolocalización

**Vistas agregadas:**

- `view_divipol_departamento` - Agrega por departamento (34 registros)
- `view_divipol_municipio` - Agrega por municipio
- `view_divipol_zona` - Agrega por zona electoral
- `view_divipol_puesto` - Agrega por puesto de votación

**Campos clave:**

- `clase`: Tipo de registro (N=Nacional, D=Departamento, M=Municipio, P=Puesto)
- `coddepto`, `codmipio`, `codzona`, `codpuesto`: Códigos jerárquicos
- `potfemenino`, `potmasculino`, `pottotal`: Potencial electoral
- `nummesas`: Número de mesas de votación

### Database Migrations

**Liquibase** manages database schema:

- Changesets: `src/main/resources/config/liquibase/`
- Master changelog: `config/liquibase/master.xml`
- Changesets divipol:
  - `20251027140000_added_entity_Divipol.xml` - Estructura tabla divipol
  - `20251027141000_added_views_Divipol.xml` - 4 vistas agregadas
  - `20251027142000_added_entity_Divipol_data.xml` - Carga 18,016 registros
- In dev mode, Liquibase runs with contexts: `dev, faker` (includes sample data)
- Generate diff changelog:
  ```bash
  ./mvnw liquibase:diff
  ```

**Nota importante sobre tipo POINT**: La columna `cordenadas` usa el tipo POINT nativo de PostgreSQL. Los changesets usan una tabla auxiliar para convertir strings a POINT durante la carga de datos desde CSV.

### Kafka Integration

The application includes Kafka producers and consumers in the `broker/` package:

- `KafkaProducer` - Sends messages to Kafka topics
- `KafkaConsumer` - Receives messages from Kafka topics
- Test resource: `TyseScrutinyMicroDivipolKafkaResource` for testing Kafka integration

## Configuration

### Profiles

- `dev` (default) - Development with hot reload, verbose logging, connects to local services
- `prod` - Production optimized build with minimal logging
- `test` / `testdev` / `testprod` - Testing profiles
- `api-docs` - Enable API documentation endpoints
- `tls` - Enable TLS/HTTPS
- `no-liquibase` - Disable Liquibase on startup

### Key Configuration Files

- `application.yml` - Base configuration
- `application-dev.yml` - Development overrides (port 5433 for PostgreSQL)
- `application-prod.yml` - Production settings
- `bootstrap.yml` - Bootstrap configuration for Consul
- `.yo-rc.json` - JHipster generator configuration
- `swagger/api.yml` - OpenAPI specification

## Testing Considerations

- Use **Testcontainers** for integration tests (PostgreSQL, Kafka)
- **ArchUnit** enforces architectural rules
- **BlockHound** detects blocking calls in reactive code
- Integration tests require Docker

## Docker

Full application stack can be containerized:

```bash
# Build Docker image
npm run java:docker

# Run full application with dependencies
docker compose -f src/main/docker/app.yml up -d
```

The application Docker image is named `tysescrutinymicrodivipol:latest` and runs on port 8081.

## Monitoring

```bash
# JHipster Control Center (monitoring dashboard)
docker compose -f src/main/docker/jhipster-control-center.yml up
# Access at http://localhost:7419

# Prometheus + Grafana monitoring stack
docker compose -f src/main/docker/monitoring.yml up

# Zipkin tracing (use -Pzipkin profile)
docker compose -f src/main/docker/zipkin.yml up
```

## Important Notes

- This is a **microservice** - it's designed to work behind an API gateway, not standalone
- CORS is disabled by default (enable in application-dev.yml if needed)
- JWT secret is configured in application-dev.yml (change for production)
- The application uses **reactive programming** patterns throughout
- Always run `./mvnw generate-sources` after modifying `swagger/api.yml`

# Scripts de CI/CD - Microservicio Divipol

Esta carpeta contiene scripts para automatizar el CI local y el push a Git del microservicio **tyse-scrutiny-micro-divipol**.

## 📜 Scripts Disponibles

### 1. `ci-local.sh` - Pipeline CI Local

Replica localmente el workflow CI que se ejecuta en GitHub Actions. Ejecuta todos los tests y validaciones del microservicio.

**Características:**

- ✅ Limpieza automática del ambiente antes de ejecutar
- ✅ Unit tests (Maven Surefire)
- ✅ Integration tests (Maven Failsafe)
- ✅ Cucumber BDD tests
- ✅ Code quality checks (Prettier, nohttp)
- ✅ Coverage reports (JaCoCo)
- ⚡ Tests de performance con Gatling (opcional)

**Uso:**

```bash
# Ejecutar CI completo (3-5 minutos)
./scripts/ci-local.sh

# Incluir tests de performance con Gatling (5-7 minutos)
./scripts/ci-local.sh --with-performance
```

**Reportes generados:**

- `target/surefire-reports/` - Resultados de unit tests
- `target/failsafe-reports/` - Resultados de integration tests
- `target/site/jacoco/` - Reporte de cobertura
- `target/cucumber-reports/` - Reportes BDD Cucumber
- `target/gatling/` - Resultados de performance (si se ejecuta con `--with-performance`)

---

### 2. `push.sh` - CI + Git Push

Ejecuta el pipeline CI completo y hace push a Git **solo si todos los tests pasan**. Garantiza que nunca pushes código roto.

**Características:**

- 🛡️ Valida código antes de push (previene builds rotos)
- 🚀 Push automático si el CI pasa
- ⏭️ Opción para saltarse CI (uso de emergencia)
- 🎯 Soporte para diferentes remotes y branches

**Uso:**

```bash
# Push a origin/current-branch con CI
./scripts/push.sh

# Push a origin/develop
./scripts/push.sh origin develop

# Push sin ejecutar CI (¡no recomendado!)
./scripts/push.sh --skip-ci

# Push con tests de performance
./scripts/push.sh --with-performance

# Combinar opciones
./scripts/push.sh --with-performance origin develop
```

**Flujo de ejecución:**

1. **Pre-flight:** Limpia ambiente (detiene Docker, mata procesos en puerto 8081)
2. **CI Pipeline:** Ejecuta todos los tests
3. **Git Push:** Solo si el CI pasó exitosamente

**Si el CI falla:**

- ❌ No se hace push
- 📋 Muestra opciones para debuggear
- 💡 Sugiere comandos para investigar

---

## 🔍 Diferencias con el Gateway

A diferencia del gateway (`tyse-scrutiny-gateway`), este microservicio **NO tiene:**

- ❌ Frontend (React)
- ❌ Tests E2E con Cypress
- ❌ Tests de npm/webpack

**Lo que SÍ tiene:**

- ✅ Backend Java con Spring WebFlux
- ✅ R2DBC + PostgreSQL (puerto 5433)
- ✅ Cucumber BDD tests
- ✅ Integration tests con Testcontainers
- ✅ Tests de performance con Gatling

---

## 🐳 Requisitos Previos

### Docker

Ambos scripts requieren **Docker** para:

- PostgreSQL (puerto 5433)
- Testcontainers (para integration tests)

**Verificar Docker:**

```bash
docker ps
```

### Servicios Compartidos

⚠️ **IMPORTANTE:** Este microservicio requiere **Consul** y **Kafka** corriendo. Estos servicios se levantan **UNA SOLA VEZ** desde el gateway:

```bash
# Desde el gateway (tyse-scrutiny-gateway)
cd ../tyse-scrutiny-gateway
docker compose -f src/main/docker/services.yml up -d

# Verificar que estén corriendo
docker ps | grep -E "consul|kafka"
```

**Configuración:**

- **Consul:** http://localhost:8500
- **Kafka:** localhost:9092
- **PostgreSQL (microservicio):** localhost:5433

---

## 🚀 Workflow Recomendado

### Desarrollo Normal

```bash
# 1. Hacer cambios en el código
vim src/main/java/com/tyse/scrutiny/micro/divipol/...

# 2. Ejecutar CI local para validar
./scripts/ci-local.sh

# 3. Si todo pasa, hacer commit
git add .
git commit -m "feat(divipol): add new feature"

# 4. Push con validación automática
./scripts/push.sh
```

### Testing Intensivo (Pre-Release)

```bash
# Ejecutar CI completo con performance tests
./scripts/ci-local.sh --with-performance

# Push con performance tests
./scripts/push.sh --with-performance
```

### Emergencia (¡Usar con precaución!)

```bash
# Push sin ejecutar CI (solo en casos de emergencia)
./scripts/push.sh --skip-ci
```

---

## 📊 Tiempos de Ejecución Típicos

| Comando                                    | Tiempo Aproximado  |
| ------------------------------------------ | ------------------ |
| `./scripts/ci-local.sh`                    | 3-5 minutos        |
| `./scripts/ci-local.sh --with-performance` | 5-7 minutos        |
| `./scripts/push.sh`                        | 3-5 minutos + push |
| `./scripts/push.sh --skip-ci`              | ~5 segundos        |

_Los tiempos varían según hardware y carga del sistema._

---

## 🧹 Limpieza del Ambiente

Ambos scripts hacen **limpieza automática** antes de ejecutar:

```bash
# Lo que se limpia:
✓ Detener contenedores Docker (PostgreSQL)
✓ Eliminar directorio target/
✓ Matar procesos Java en puerto 8081
✓ Limpiar archivos temporales
```

Esta limpieza garantiza condiciones iniciales consistentes, igual que en CI/CD.

---

## 🐛 Troubleshooting

### Error: "Cannot connect to Consul"

**Causa:** Consul no está corriendo

**Solución:**

```bash
cd ../tyse-scrutiny-gateway
docker compose -f src/main/docker/consul.yml up -d
# Verificar: http://localhost:8500
```

### Error: "Port 8081 already in use"

**Causa:** Otra instancia del microservicio está corriendo

**Solución:**

```bash
# Encontrar y matar proceso
lsof -i :8081
kill -9 [PID]

# O dejar que el script lo haga automáticamente (pre-flight cleaning)
```

### Error: "PostgreSQL connection refused"

**Causa:** PostgreSQL del microservicio no está corriendo

**Solución:**

```bash
docker compose -f src/main/docker/postgresql.yml up -d
# Verificar: docker ps | grep postgres
```

### Tests fallan con "Testcontainers timeout"

**Causa:** Docker no está corriendo o tiene recursos limitados

**Solución:**

```bash
# Verificar Docker
docker ps

# Aumentar recursos de Docker Desktop (si usas Mac/Windows)
# Settings → Resources → Memory (mínimo 4GB recomendado)
```

---

## 📝 Notas Importantes

1. **No ejecutar en paralelo:** No ejecutes `ci-local.sh` múltiples veces en paralelo, compiten por el mismo puerto 8081.

2. **Kafka/Consul compartidos:** Estos servicios se levantan desde el gateway, NO desde este microservicio.

3. **Clean builds:** Los scripts siempre hacen `clean` antes de ejecutar para garantizar consistencia.

4. **Reactive stack:** Este microservicio usa Spring WebFlux, R2DBC y programación reactiva. Los tests usan BlockHound para detectar bloqueos.

5. **Cucumber BDD:** Los tests BDD se ejecutan automáticamente con `./mvnw verify`.

---

## 🔗 Enlaces Útiles

- **JHipster Docs:** https://www.jhipster.tech/documentation-archive/v8.11.0
- **CLAUDE.md principal:** `/home/manuel-motta/repos/tyse/CLAUDE.md`
- **CLAUDE.md microservicio:** `/home/manuel-motta/repos/tyse/tyse-scrutiny-micro-divipol/CLAUDE.md`

---

## 📜 Licencia

Scripts generados para el proyecto **Tyse Scrutiny - Microservicio Divipol**.

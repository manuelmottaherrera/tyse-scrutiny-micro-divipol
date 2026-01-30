#!/bin/bash

################################################################################
# CI Local - Microservicio Divipol CI Pipeline
#
# Este script replica localmente el workflow CI del microservicio.
# Ejecuta los mismos comandos que se ejecutan en GitHub Actions.
#
# IMPORTANTE: Este script limpia el ambiente CI antes de ejecutar los tests
# para garantizar condiciones iniciales consistentes. Esto incluye:
#   - Eliminar directorio target/
#   - Detener contenedores Docker de ejecuciones anteriores
#   - Matar procesos Java remanentes en puerto 8081
#
# A diferencia del gateway, este microservicio NO tiene:
#   - Frontend (React)
#   - Tests E2E (Cypress)
#
# Los tests que SÍ se ejecutan:
#   - Unit tests (Maven Surefire)
#   - Integration tests (Maven Failsafe)
#   - Cucumber BDD tests
#   - Checkstyle y code quality
#
# Uso:
#   ./scripts/ci-local.sh [--with-performance]
#
# Opciones:
#   --with-performance    Incluye tests de performance con Gatling (añade ~5 minutos)
################################################################################

set -e  # Exit on error
set -o pipefail  # Ensure pipe commands return the exit code of the failing command

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Parse arguments
RUN_PERFORMANCE=false
if [[ "$1" == "--with-performance" ]]; then
    RUN_PERFORMANCE=true
fi

# Calculate total jobs based on options
if [ "$RUN_PERFORMANCE" = true ]; then
    TOTAL_JOBS=3
else
    TOTAL_JOBS=2
fi

# Track timing
SCRIPT_START=$(date +%s)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Microservicio Divipol - CI Pipeline${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

################################################################################
# Pre-flight: Clean Environment
################################################################################

echo -e "${YELLOW}[Pre-flight] Cleaning CI environment...${NC}"
CLEAN_START=$(date +%s)

# 1. Detener y limpiar contenedores Docker de ejecuciones anteriores
echo "  → Stopping and removing Docker containers from previous runs..."
docker compose -f src/main/docker/postgresql.yml down -v 2>/dev/null || true
docker compose -f src/main/docker/kafka.yml down -v 2>/dev/null || true
docker compose -f src/main/docker/consul.yml down -v 2>/dev/null || true

# 2. Limpiar redes y contenedores huérfanos de Docker (evita errores de red no encontrada)
echo "  → Cleaning orphaned Docker networks and containers..."
docker container prune -f > /dev/null 2>&1 || true
docker network prune -f > /dev/null 2>&1 || true

# 3. Limpiar directorio target/ (artefactos de Maven)
if [ -d "target/" ]; then
    echo "  → Removing target/ directory..."
    rm -rf target/
fi

# 4. Limpiar archivos temporales de build
echo "  → Removing temporary build files..."
rm -rf build/ 2>/dev/null || true

# 5. Matar procesos Java remanentes (del puerto 8081 - microservicio)
JAVA_PID=$(lsof -ti:8081 2>/dev/null || true)
if [ -n "$JAVA_PID" ]; then
    echo "  → Killing Java process on port 8081 (PID: $JAVA_PID)..."
    kill -9 $JAVA_PID 2>/dev/null || true
    sleep 2
fi

CLEAN_END=$(date +%s)
CLEAN_TIME=$((CLEAN_END - CLEAN_START))

echo -e "${GREEN}✓ Environment cleaned${NC} (${CLEAN_TIME}s)"
echo ""

################################################################################
# Start Docker Services (PostgreSQL)
################################################################################

echo -e "${YELLOW}[Services] Starting Docker services...${NC}"
SERVICES_START=$(date +%s)

mkdir -p logs
echo "  → Starting PostgreSQL (port 5433)..."
docker compose -f src/main/docker/postgresql.yml up -d > logs/services-start.log 2>&1

# Esperar a que PostgreSQL esté healthy
echo -n "    PostgreSQL: "
RETRIES=30
until docker compose -f src/main/docker/postgresql.yml ps postgresql 2>/dev/null | grep -q "healthy" || [ $RETRIES -eq 0 ]; do
    echo -n "."
    sleep 2
    RETRIES=$((RETRIES - 1))
done
if [ $RETRIES -gt 0 ]; then
    echo -e " ${GREEN}healthy${NC}"
else
    echo -e " ${RED}timeout${NC}"
    echo "    Check logs/services-start.log for details"
    exit 1
fi

SERVICES_END=$(date +%s)
SERVICES_TIME=$((SERVICES_END - SERVICES_START))

echo -e "${GREEN}✓ Docker services ready${NC} (${SERVICES_TIME}s)"
echo ""

################################################################################
# Liquibase Verification (Update → Rollback → Update)
################################################################################

echo -e "${YELLOW}[Liquibase] Verifying migrations and rollback capability...${NC}"
ROLLBACK_START=$(date +%s)

# Compilar proyecto (necesario para que Liquibase encuentre los recursos)
echo "  → Compiling project for Liquibase..."
if ! ./mvnw compile -DskipTests -q > logs/liquibase-compile.log 2>&1; then
    echo -e "    ${RED}✗ Error: Compilation failed${NC}"
    echo "    Check logs/liquibase-compile.log for details"
    tail -20 logs/liquibase-compile.log
    exit 1
fi

# Paso 1: Aplicar todos los changesets
echo "  → Step 1: Applying all changesets with liquibase:update..."
if ./mvnw liquibase:update \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.liquibase=INFO \
  > logs/liquibase-update-1.log 2>&1; then

    APPLIED=$(grep -c "ChangeSet.*ran successfully" logs/liquibase-update-1.log 2>/dev/null || echo "0")
    echo -e "    ${GREEN}✓ Applied $APPLIED changeset(s) successfully${NC}"
else
    echo -e "    ${RED}✗ Error: Failed to apply changesets${NC}"
    echo "    Check logs/liquibase-update-1.log for details"
    tail -20 logs/liquibase-update-1.log
    exit 1
fi

# Paso 2: Rollback hasta el tag inicial
echo "  → Step 2: Testing rollback to tag 'estado-vacio'..."
if ./mvnw liquibase:rollback -Dliquibase.rollbackTag=estado-vacio \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.liquibase=INFO \
  > logs/liquibase-rollback.log 2>&1; then

    ROLLED_BACK=$(grep -c "Rolling Back Changeset" logs/liquibase-rollback.log 2>/dev/null || echo "0")
    echo -e "    ${GREEN}✓ Rolled back $ROLLED_BACK changeset(s) successfully${NC}"
else
    echo -e "    ${RED}✗ Error: Rollback failed${NC}"
    echo "    Check logs/liquibase-rollback.log for details"
    tail -20 logs/liquibase-rollback.log
    exit 1
fi

# Paso 3: Re-aplicar todos los changesets (dejar BD lista para tests)
echo "  → Step 3: Re-applying all changesets..."
if ./mvnw liquibase:update \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.liquibase=INFO \
  > logs/liquibase-update-2.log 2>&1; then

    REAPPLIED=$(grep -c "ChangeSet.*ran successfully" logs/liquibase-update-2.log 2>/dev/null || echo "0")
    echo -e "    ${GREEN}✓ Re-applied $REAPPLIED changeset(s) - Database ready${NC}"
else
    echo -e "    ${RED}✗ Error: Failed to re-apply changesets${NC}"
    echo "    Check logs/liquibase-update-2.log for details"
    tail -20 logs/liquibase-update-2.log
    exit 1
fi

ROLLBACK_END=$(date +%s)
ROLLBACK_TIME=$((ROLLBACK_END - ROLLBACK_START))
echo -e "${GREEN}✓ Liquibase verification passed${NC} (${ROLLBACK_TIME}s)"
echo ""

################################################################################
# Job 1: Backend Tests (Unit + Integration + BDD)
################################################################################

echo -e "${YELLOW}[Job 1/${TOTAL_JOBS}] Starting Backend Tests...${NC}"
BACKEND_START=$(date +%s)

echo "  → Running Maven verify (unit + integration + BDD)..."
echo "     - Unit tests (Surefire)"
echo "     - Integration tests (Failsafe)"
echo "     - Cucumber BDD tests"
echo "     - Checkstyle validation"
echo ""

./mvnw clean verify \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.tech.jhipster=ERROR \
  -Dlogging.level.com.tyse.scrutiny=ERROR

BACKEND_END=$(date +%s)
BACKEND_TIME=$((BACKEND_END - BACKEND_START))

echo ""
echo -e "${GREEN}✓ Backend Tests passed${NC} (${BACKEND_TIME}s)"
echo ""

# Check if test results exist
if [ -d "target/surefire-reports/" ]; then
    UNIT_TESTS=$(find target/surefire-reports/ -name "TEST-*.xml" | wc -l)
    echo "  → Unit test results: target/surefire-reports/ (${UNIT_TESTS} test suites)"
fi

if [ -d "target/failsafe-reports/" ]; then
    IT_TESTS=$(find target/failsafe-reports/ -name "TEST-*.xml" 2>/dev/null | wc -l)
    if [ "$IT_TESTS" -gt 0 ]; then
        echo "  → Integration test results: target/failsafe-reports/ (${IT_TESTS} test suites)"
    fi
fi

if [ -d "target/site/jacoco/" ]; then
    echo "  → Coverage report: target/site/jacoco/index.html"
fi

if [ -d "target/cucumber-reports/" ]; then
    echo "  → Cucumber BDD reports: target/cucumber-reports/"
fi

echo ""

################################################################################
# Job 2: Code Quality Checks
################################################################################

echo -e "${YELLOW}[Job 2/${TOTAL_JOBS}] Starting Code Quality Checks...${NC}"
QUALITY_START=$(date +%s)

echo "  → Running Prettier format check..."
npm run prettier:check

echo "  → Running nohttp check (detecting http:// URLs)..."
npm run backend:nohttp:test

QUALITY_END=$(date +%s)
QUALITY_TIME=$((QUALITY_END - QUALITY_START))

echo -e "${GREEN}✓ Code Quality Checks passed${NC} (${QUALITY_TIME}s)"
echo ""

################################################################################
# Job 3: Performance Tests (Optional)
################################################################################

PERFORMANCE_TIME=0
PERFORMANCE_PASSED=false
if [ "$RUN_PERFORMANCE" = true ]; then
    echo -e "${YELLOW}[Job 3/${TOTAL_JOBS}] Starting Performance Tests...${NC}"
    PERFORMANCE_START=$(date +%s)

    # 1. Levantar Consul (requerido para service discovery)
    echo "  → Starting Consul for service discovery..."
    if ! docker compose -f src/main/docker/consul.yml up -d > logs/consul-start.log 2>&1; then
        echo -e "    ${RED}✗ Failed to start Consul${NC}"
        echo "    Check logs/consul-start.log for details:"
        tail -10 logs/consul-start.log
        exit 1
    fi

    # Esperar a que Consul esté listo (max 30 segundos)
    echo -n "    Consul: "
    CONSUL_RETRIES=15
    CONSUL_READY=false
    while [ $CONSUL_RETRIES -gt 0 ]; do
        if curl -s http://localhost:8500/v1/status/leader 2>/dev/null | grep -q ":8300"; then
            CONSUL_READY=true
            break
        fi
        echo -n "."
        sleep 2
        CONSUL_RETRIES=$((CONSUL_RETRIES - 1))
    done

    if [ "$CONSUL_READY" = true ]; then
        echo -e " ${GREEN}ready${NC}"
    else
        echo -e " ${YELLOW}started (may take a moment to be fully ready)${NC}"
    fi

    # 2. Levantar Kafka (requerido para messaging)
    echo "  → Starting Kafka for messaging..."
    if ! docker compose -f src/main/docker/kafka.yml up -d > logs/kafka-start.log 2>&1; then
        echo -e "    ${RED}✗ Failed to start Kafka${NC}"
        echo "    Check logs/kafka-start.log for details:"
        tail -10 logs/kafka-start.log
        echo ""
        echo -e "    ${YELLOW}Hint: Try running 'docker container prune -f && docker network prune -f' to clean up orphaned resources${NC}"
        exit 1
    fi

    # Esperar a que Kafka esté listo (max 30 segundos)
    echo -n "    Kafka: "
    KAFKA_RETRIES=15
    KAFKA_READY=false
    while [ $KAFKA_RETRIES -gt 0 ]; do
        if docker compose -f src/main/docker/kafka.yml ps kafka 2>/dev/null | grep -q "Up"; then
            # Verificar que el broker responde
            if docker compose -f src/main/docker/kafka.yml exec -T kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 > /dev/null 2>&1; then
                KAFKA_READY=true
                break
            fi
        fi
        echo -n "."
        sleep 2
        KAFKA_RETRIES=$((KAFKA_RETRIES - 1))
    done

    if [ "$KAFKA_READY" = true ]; then
        echo -e " ${GREEN}ready${NC}"
    else
        echo -e " ${YELLOW}started (may take a moment to be fully ready)${NC}"
    fi

    # 3. Levantar el servidor en background
    echo "  → Starting application server on port 8081..."
    ./mvnw spring-boot:run \
      -Dspring-boot.run.profiles=dev,no-liquibase \
      -Dlogging.level.ROOT=WARN \
      -Dlogging.level.com.tyse.scrutiny=INFO \
      > logs/server-performance.log 2>&1 &
    SERVER_PID=$!
    echo "    Server PID: $SERVER_PID"

    # 3. Esperar a que el servidor esté listo (max 120 segundos)
    echo -n "    Waiting for server to be ready"
    RETRIES=60
    SERVER_READY=false
    while [ $RETRIES -gt 0 ]; do
        if curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/management/health 2>/dev/null | grep -q "200"; then
            SERVER_READY=true
            break
        fi
        echo -n "."
        sleep 2
        RETRIES=$((RETRIES - 1))
    done
    echo ""

    if [ "$SERVER_READY" = true ]; then
        echo -e "    ${GREEN}Server is ready${NC}"
        echo ""

        # 4. Ejecutar Gatling
        echo "  → Running Gatling performance tests..."
        echo "     - Load testing with 10 users"
        echo "     - Testing all divipol API endpoints"
        echo ""

        if ./mvnw gatling:test -Dusers=10 -Dramp=1 > logs/gatling-output.log 2>&1; then
            # 5. Verificar que los tests realmente pasaron
            # Gatling muestra estadísticas como: > request count | Total | OK | KO
            # Extraemos los números de OK y KO para calcular el porcentaje de éxito

            # Extraer estadísticas de request count
            REQUEST_LINE=$(grep "^> request count" logs/gatling-output.log 2>/dev/null | head -1)
            if [ -n "$REQUEST_LINE" ]; then
                # Formato: > request count | TOTAL | OK | KO
                TOTAL=$(echo "$REQUEST_LINE" | awk -F'|' '{gsub(/[^0-9]/,"",$2); print $2}')
                OK=$(echo "$REQUEST_LINE" | awk -F'|' '{gsub(/[^0-9]/,"",$3); print $3}')
                KO=$(echo "$REQUEST_LINE" | awk -F'|' '{gsub(/[^0-9]/,"",$4); print $4}')

                # Calcular porcentaje de éxito (evitar división por cero)
                if [ "$TOTAL" -gt 0 ]; then
                    SUCCESS_RATE=$((OK * 100 / TOTAL))
                else
                    SUCCESS_RATE=0
                fi

                echo "    Request stats: $OK OK / $TOTAL total ($SUCCESS_RATE% success rate)"

                # Aceptar si el éxito es >= 90%
                if [ "$SUCCESS_RATE" -ge 90 ]; then
                    PERFORMANCE_PASSED=true
                    echo -e "    ${GREEN}✓ Performance test passed ($SUCCESS_RATE% success rate)${NC}"
                    # Mostrar resumen de estadísticas
                    grep -E "^> (mean response time|response time 95th)" logs/gatling-output.log | head -3

                    # Mostrar advertencia si hubo algunos errores
                    if [ "$KO" -gt 0 ]; then
                        echo -e "    ${YELLOW}⚠ $KO requests failed (acceptable for warmup)${NC}"
                    fi
                else
                    PERFORMANCE_PASSED=false
                    echo -e "    ${RED}✗ Performance test failed ($SUCCESS_RATE% success rate, need >= 90%)${NC}"
                    echo "    Error summary:"
                    grep -A5 "^---- Errors" logs/gatling-output.log | grep "^>" | head -5
                fi
            else
                # No se encontraron estadísticas - algo salió muy mal
                PERFORMANCE_PASSED=false
                echo -e "    ${RED}✗ Could not parse Gatling results${NC}"
                tail -20 logs/gatling-output.log
            fi
        else
            echo -e "    ${RED}✗ Gatling execution failed${NC}"
            tail -20 logs/gatling-output.log
            PERFORMANCE_PASSED=false
        fi
    else
        echo -e "    ${RED}Server failed to start within 120 seconds${NC}"
        echo "    Check logs/server-performance.log for details"
        tail -30 logs/server-performance.log
        PERFORMANCE_PASSED=false
    fi

    # 6. Detener el servidor
    echo ""
    echo "  → Stopping application server..."
    if [ -n "$SERVER_PID" ] && kill -0 $SERVER_PID 2>/dev/null; then
        kill $SERVER_PID 2>/dev/null || true
        sleep 2
        # Force kill si sigue corriendo
        kill -9 $SERVER_PID 2>/dev/null || true
    fi
    # También matar cualquier proceso en el puerto 8081
    JAVA_PID=$(lsof -ti:8081 2>/dev/null || true)
    if [ -n "$JAVA_PID" ]; then
        kill -9 $JAVA_PID 2>/dev/null || true
    fi
    echo "    Server stopped"

    # 7. Detener Kafka
    echo "  → Stopping Kafka..."
    docker compose -f src/main/docker/kafka.yml down -v 2>/dev/null || true
    echo "    Kafka stopped"

    echo "  → Stopping Consul..."
    docker compose -f src/main/docker/consul.yml down -v 2>/dev/null || true
    echo "    Consul stopped"

    PERFORMANCE_END=$(date +%s)
    PERFORMANCE_TIME=$((PERFORMANCE_END - PERFORMANCE_START))

    echo ""
    if [ "$PERFORMANCE_PASSED" = true ]; then
        echo -e "${GREEN}✓ Performance Tests passed${NC} (${PERFORMANCE_TIME}s)"
    else
        echo -e "${RED}✗ Performance Tests failed${NC} (${PERFORMANCE_TIME}s)"
    fi

    # Check if test results exist
    if [ -d "target/gatling/" ]; then
        GATLING_REPORT=$(find target/gatling/ -name "index.html" -type f 2>/dev/null | head -1)
        if [ -n "$GATLING_REPORT" ]; then
            echo "  → Performance report: ${GATLING_REPORT}"
        fi
    fi
    echo ""

    # Fallar el CI si performance falló
    if [ "$PERFORMANCE_PASSED" = false ]; then
        echo -e "${RED}========================================${NC}"
        echo -e "${RED}❌ CI Pipeline Failed - Performance Tests${NC}"
        echo -e "${RED}========================================${NC}"
        exit 1
    fi
fi

################################################################################
# Quality Gate
################################################################################

echo -e "${YELLOW}Quality Gate Check...${NC}"
echo "  Backend Tests: ${GREEN}success${NC}"
echo "  Code Quality:  ${GREEN}success${NC}"

if [ "$RUN_PERFORMANCE" = true ]; then
    echo "  Performance:   ${GREEN}success${NC}"
fi

echo ""

################################################################################
# Summary
################################################################################

SCRIPT_END=$(date +%s)
TOTAL_TIME=$((SCRIPT_END - SCRIPT_START))

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ CI Pipeline Passed${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Timing Summary:"
echo "  Environment Cleanup:     ${CLEAN_TIME}s"
echo "  Docker Services:         ${SERVICES_TIME}s"
echo "  Liquibase Verification:  ${ROLLBACK_TIME}s"
echo "  Backend Tests:           ${BACKEND_TIME}s"
echo "  Code Quality:            ${QUALITY_TIME}s"
if [ "$RUN_PERFORMANCE" = true ]; then
    echo "  Performance Tests:       ${PERFORMANCE_TIME}s"
fi
echo "  ───────────────────────────────────"
echo "  Total:                   ${TOTAL_TIME}s"
echo ""
echo "All checks passed! ✓"
echo "The code is ready to be pushed to GitHub."
echo ""

################################################################################
# Post-flight: Cleanup Background Processes
################################################################################

# Stop all Docker services (PostgreSQL)
echo -e "${YELLOW}[Post-flight] Cleaning up Docker services...${NC}"
docker compose -f src/main/docker/postgresql.yml down -v 2>/dev/null || true

# Wait for any background jobs to finish
wait

# Ensure no orphaned Java processes are running
JAVA_PID=$(lsof -ti:8081 2>/dev/null || true)
if [ -n "$JAVA_PID" ]; then
    kill -9 $JAVA_PID 2>/dev/null || true
fi

# Explicit exit to ensure proper return to calling process (git push hook)
exit 0

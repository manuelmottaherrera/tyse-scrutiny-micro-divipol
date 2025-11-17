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

# 2. Limpiar directorio target/ (artefactos de Maven)
if [ -d "target/" ]; then
    echo "  → Removing target/ directory..."
    rm -rf target/
fi

# 3. Limpiar archivos temporales de build
echo "  → Removing temporary build files..."
rm -rf build/ 2>/dev/null || true

# 4. Matar procesos Java remanentes (del puerto 8081 - microservicio)
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
# Job 1: Backend Tests (Unit + Integration + BDD)
################################################################################

echo -e "${YELLOW}[Job 1/2] Starting Backend Tests...${NC}"
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

echo -e "${YELLOW}[Job 2/2] Starting Code Quality Checks...${NC}"
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
if [ "$RUN_PERFORMANCE" = true ]; then
    echo -e "${YELLOW}[Job 3/3] Starting Performance Tests...${NC}"
    PERFORMANCE_START=$(date +%s)

    echo "  → Running Gatling performance tests..."
    ./mvnw gatling:test

    PERFORMANCE_END=$(date +%s)
    PERFORMANCE_TIME=$((PERFORMANCE_END - PERFORMANCE_START))

    echo -e "${GREEN}✓ Performance Tests passed${NC} (${PERFORMANCE_TIME}s)"
    echo ""

    # Check if test results exist
    if [ -d "target/gatling/" ]; then
        echo "  → Performance results: target/gatling/"
    fi
    echo ""
else
    echo -e "${BLUE}[Job 3/3] Performance Tests skipped${NC} (use --with-performance to run)"
    echo ""
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
echo "  Environment Cleanup: ${CLEAN_TIME}s"
echo "  Backend Tests:       ${BACKEND_TIME}s"
echo "  Code Quality:        ${QUALITY_TIME}s"
if [ "$RUN_PERFORMANCE" = true ]; then
    echo "  Performance Tests:   ${PERFORMANCE_TIME}s"
fi
echo "  ─────────────────────────────"
echo "  Total:               ${TOTAL_TIME}s"
echo ""
echo "All checks passed! ✓"
echo "The code is ready to be pushed to GitHub."
echo ""

################################################################################
# Post-flight: Cleanup Background Processes
################################################################################

# Wait for any background jobs to finish
wait

# Ensure no orphaned Java processes are running
JAVA_PID=$(lsof -ti:8081 2>/dev/null || true)
if [ -n "$JAVA_PID" ]; then
    kill -9 $JAVA_PID 2>/dev/null || true
fi

# Explicit exit to ensure proper return to calling process (git push hook)
exit 0

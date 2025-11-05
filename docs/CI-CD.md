# CI/CD - Divipol Microservice

This repository is part of the Tyse Scrutiny microservices platform.

## Repository Structure

Tyse Scrutiny uses **two separate repositories**:

1. **Gateway** (github.com/manuelmottaherrera/tyse-scrutiny-gateway)

   - Frontend + Backend + Deployment orchestration

2. **Divipol** (THIS REPOSITORY)
   - Microservice for Colombian geographic data

---

## CI/CD Workflows

This repository contains the following GitHub Actions workflows:

### 1. CI Pipeline (`.github/workflows/ci.yml`)

**Trigger**: Every push and pull request to `main` or `develop`

**What it does**:

- ✅ Runs backend tests (Maven + JUnit)
- ✅ Verifies code quality
- ✅ Quality gate check

**Duration**: ~10 minutes

### 2. Build Docker Image (`.github/workflows/build.yml`)

**Trigger**: After CI success on `main` or `develop`

**What it does**:

- 🏗️ Builds production JAR
- 🐳 Creates Docker image
- 📦 Pushes to GitHub Container Registry

**Image**: `ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol`

**Tags**:

- `develop` - Latest develop branch
- `main` - Latest main branch
- `latest` - Latest stable release (main only)
- `develop-sha-abc123` - Specific commit

**Duration**: ~5 minutes

### 3. SonarCloud Analysis (`.github/workflows/sonarcloud.yml`)

**Trigger**: Every push and pull request (parallel with CI)

**What it does**:

- 🔍 Code quality analysis
- 📊 Test coverage tracking
- 🔒 Security vulnerability scanning

**Duration**: ~5-8 minutes

---

## Development Workflow

### 1. Local Development

```bash
# Clone repository
git clone git@github.com:manuelmottaherrera/tyse-scrutiny-micro-divipol.git
cd tyse-scrutiny-micro-divipol

# Create feature branch
git checkout -b feature/my-feature

# Run tests
./mvnw verify

# Commit and push
git add .
git commit -m "feat: add new feature"
git push origin feature/my-feature
```

### 2. CI Runs Automatically

Once you push, GitHub Actions will:

1. Run tests
2. Analyze code quality
3. If tests pass, build Docker image
4. Push image to GitHub Container Registry

### 3. Deployment

**Important**: Deployment is **NOT handled in this repository**.

Deployment is managed by the **Gateway repository** because:

- Gateway owns the shared infrastructure (Consul, Kafka)
- Gateway orchestrates the deployment of all services
- Deployment requires coordination between Gateway and Divipol

**To deploy Divipol**:

1. Wait for build to complete (image pushed to GHCR)
2. Go to **Gateway repository**: https://github.com/manuelmottaherrera/tyse-scrutiny-gateway
3. Navigate to **Actions** → **Deploy to Staging**
4. Run workflow with desired image tags

---

## Docker Image

### Image Tags

After a successful build, images are available at:

```
ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol:develop
ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol:main
ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol:latest
ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol:develop-sha-abc123
```

### Pull Image Locally

```bash
# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u your-username --password-stdin

# Pull image
docker pull ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol:develop

# Run locally
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=dev \
  ghcr.io/manuelmottaherrera/tyse-scrutiny-micro-divipol:develop
```

---

## GitHub Secrets Required

This repository requires the following GitHub Secrets:

| Secret                      | Description                        | Used By             |
| --------------------------- | ---------------------------------- | ------------------- |
| `SONAR_TOKEN`               | SonarCloud authentication token    | SonarCloud workflow |
| `SONAR_ORGANIZATION`        | SonarCloud organization key        | SonarCloud workflow |
| `SONAR_PROJECT_KEY_DIVIPOL` | SonarCloud project key for Divipol | SonarCloud workflow |

**Setup Instructions**: See [Gateway CI/CD Setup Guide](https://github.com/manuelmottaherrera/tyse-scrutiny-gateway/blob/develop/docs/ci-cd/SETUP.md)

---

## Viewing Workflow Status

### GitHub Actions

View workflows at:
https://github.com/manuelmottaherrera/tyse-scrutiny-micro-divipol/actions

### SonarCloud Dashboard

View code quality at:
https://sonarcloud.io/project/overview?id=tyse-scrutiny-micro-divipol

---

## Troubleshooting

### CI Fails

**Problem**: Tests fail in GitHub Actions

**Solution**:

```bash
# Run tests locally
./mvnw clean verify

# Check specific test
./mvnw test -Dtest=MyTest
```

### Build Fails

**Problem**: Docker image build fails

**Solution**:

- Ensure Dockerfile exists: `src/main/docker/Dockerfile`
- Test production build locally: `./mvnw -Pprod clean verify -DskipTests`
- Check GitHub Actions logs for details

### SonarCloud Fails

**Problem**: SonarCloud analysis fails

**Solution**:

- Verify `SONAR_TOKEN` secret is set
- Check SonarCloud project exists
- Ensure tests generate coverage reports

---

## Integration with Gateway

This microservice is designed to work with the Gateway:

**Communication**:

- Gateway → Divipol: HTTP REST (via Consul service discovery)
- Divipol → Gateway: Kafka events (asynchronous)

**Shared Infrastructure** (managed by Gateway):

- Consul (service discovery)
- Kafka (message broker)
- PostgreSQL (separate databases)

**For more details**: See [Multi-Repo Coordination Guide](https://github.com/manuelmottaherrera/tyse-scrutiny-gateway/blob/develop/docs/ci-cd/MULTI-REPO-COORDINATION.md)

---

## Complete CI/CD Documentation

For complete CI/CD documentation, including:

- Full setup guide
- Deployment procedures
- Server configuration
- Multi-repository coordination
- Architecture details

**Visit the Gateway repository documentation**:
https://github.com/manuelmottaherrera/tyse-scrutiny-gateway/tree/develop/docs/ci-cd

---

## Quick Links

- **Gateway Repository**: https://github.com/manuelmottaherrera/tyse-scrutiny-gateway
- **CI/CD Setup Guide**: https://github.com/manuelmottaherrera/tyse-scrutiny-gateway/blob/develop/docs/ci-cd/SETUP.md
- **Multi-Repo Guide**: https://github.com/manuelmottaherrera/tyse-scrutiny-gateway/blob/develop/docs/ci-cd/MULTI-REPO-COORDINATION.md
- **SonarCloud**: https://sonarcloud.io/organizations/manuelmottaherrera

---

**Last Updated**: 2025-01-04

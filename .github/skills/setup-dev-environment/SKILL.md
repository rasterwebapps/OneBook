---
name: setup-dev-environment
description: >-
  Set up and manage the OneBook local development environment using Docker Compose.
  Includes PostgreSQL 17, Redis 7, Keycloak 24, OpenLDAP, and application services.
---

# Setup Dev Environment

Set up and manage the OneBook local development environment.

## When to Use

- Setting up a new development machine
- Starting/stopping infrastructure services
- Adding a new Docker service to the stack
- Troubleshooting infrastructure issues
- Modifying CI/CD pipeline

## Steps

### 1. Start Infrastructure Only (Most Common)

For local development, start only the infrastructure services:

```bash
docker compose up -d postgres redis openldap keycloak
```

This starts:
- **PostgreSQL 17** — Primary database (port 5432)
- **Redis 7** — Warm cache (port 6379)
- **OpenLDAP** — Directory service (port 389)
- **Keycloak 24** — OIDC provider (port 8180)

### 2. Start Full Stack

To run all services including backend and frontend containers:

```bash
docker compose up -d
```

Services:
| Service | Port | Health Check |
|---------|------|-------------|
| PostgreSQL | 5432 | `pg_isready` |
| Redis | 6379 | `redis-cli ping` |
| OpenLDAP | 389 | LDAP bind |
| phpLDAPadmin | 8443 | HTTP |
| Keycloak | 8180 | HTTP `/health` |
| Backend | 8080 | HTTP `/actuator/health` |
| Frontend | 80 | HTTP |

### 3. Verify Services

```bash
# Check all containers
docker compose ps

# Check PostgreSQL
docker compose exec postgres pg_isready

# Check Redis
docker compose exec redis redis-cli ping

# Check logs
docker compose logs -f --tail=50 {service}
```

### 4. Run Backend Locally (Outside Docker)

```bash
cd backend
./gradlew bootRun
```

Requires PostgreSQL and Redis running (from Step 1).

### 5. Run Frontend Locally (Outside Docker)

```bash
cd frontend
npm install
npm start
```

Angular dev server at `http://localhost:4200`.

### 6. Adding a New Service

When adding a new Docker service:

1. Add service definition to `docker-compose.yml`
2. Add configuration files to `infrastructure/{service}/`
3. Add health check
4. Update `infrastructure/README.md`
5. Test: `docker compose config` to validate YAML

### 7. CI/CD Pipeline

The CI pipeline (`.github/workflows/ci.yml`) runs 5 jobs:

1. **validate-ownership** — Agent ownership check
2. **validate-quality-gates** — 8-gate quality validation
3. **sync-memory-bank** — Auto-sync on main pushes
4. **backend** — Build + test (Gradle)
5. **frontend** — Build + test (Angular)

### 8. Spring Configuration Profiles

| Profile | Database | Flyway | Use |
|---------|----------|--------|-----|
| default | PostgreSQL | Enabled | Local dev, production |
| test | H2 in-memory | Disabled | Unit tests |

## References

- Infrastructure agent: `.github/agents/infra.agent.md`
- Docker Compose: `docker-compose.yml`
- Infrastructure configs: `infrastructure/`
- Developer guide: `docs/technical/developer-guide.md`

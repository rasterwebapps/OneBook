# Infrastructure Services

All third-party infrastructure services used by OneBook are configured here.
Each sub-directory maps to a single Docker Compose service.

## Directory Layout

```
infrastructure/
├── postgres/              PostgreSQL 17 — primary data store
│   └── init/              Initialisation scripts (run once on first start)
│       └── 01-init-extensions.sql
├── redis/                 Redis 7 — warm cache (Cache-Aside pattern)
│   └── redis.conf         Custom configuration
├── keycloak/              Keycloak 24 — OIDC / OAuth 2.0 identity provider
│   ├── realms/            Realm configuration (auto-imported on start)
│   │   └── onebook-realm.json
│   └── themes/            Custom login theme
│       └── onebook/
└── ldap/                  OpenLDAP 1.5 — user directory (Keycloak federation)
    └── bootstrap/         LDIF files loaded on first start
        └── 01-base-structure.ldif
```

## Services

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| **postgres** | `postgres:17-alpine` | 5433 → 5432 | Primary relational data store with RLS |
| **redis** | `redis:7-alpine` | 6379 | Warm cache, Cache-Aside pattern |
| **openldap** | `osixia/openldap:1.5.0` | 389, 636 | User directory (federation source for Keycloak) |
| **ldapadmin** | `osixia/phpldapadmin:0.9.0` | 8081 | LDAP management UI (development only) |
| **keycloak** | `quay.io/keycloak/keycloak:24.0` | 8180 | OIDC identity provider |

## Quick Start

```bash
# Start all infrastructure services
docker compose up -d

# Verify health
docker compose ps

# View logs for a specific service
docker compose logs -f keycloak
```

## Service Details

### PostgreSQL

- **Database:** `onebook`
- **User / Password:** `onebook` / `onebook_secret`
- **External port:** `5433` (avoids conflict with a local PostgreSQL on 5432)
- Init scripts in `postgres/init/` run automatically on first container creation.

### Redis

- Uses a custom `redis.conf` with `allkeys-lru` eviction and AOF persistence.
- Max memory: 256 MB (configurable in `redis/redis.conf`).

### Keycloak

- Admin console: <http://localhost:8180>
- Credentials: `admin` / `admin_secret`
- The `onebook` realm is auto-imported from `keycloak/realms/onebook-realm.json`.
- Custom login theme is mounted from `keycloak/themes/onebook/`.
- Federated with OpenLDAP — does **not** store user passwords locally.

### OpenLDAP

- Base DN: `dc=onebook,dc=local`
- Admin bind: `cn=admin,dc=onebook,dc=local` / `ldap_admin_secret`
- Bootstrap LDIF files in `ldap/bootstrap/` create organisational units, groups, and a default admin user.
- phpLDAPadmin UI available at <http://localhost:8081> for development.

## Customisation

- **Add LDAP users:** Create additional `.ldif` files in `ldap/bootstrap/`. Files are processed in alphabetical order.
- **Change Keycloak theme:** Edit CSS in `keycloak/themes/onebook/login/resources/css/`.
- **Modify realm settings:** Edit `keycloak/realms/onebook-realm.json` and restart Keycloak.
- **Tune Redis:** Edit `redis/redis.conf` and restart the Redis container.

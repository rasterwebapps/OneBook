# AI Memory — Authentication Architecture

> **Purpose:** Persistent documentation for AI agents about authentication flow and security decisions.  
> **Last Updated:** 2026-04-02

---

## Authentication Flow Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           AUTHENTICATION FLOW                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    OIDC/OAuth2    ┌──────────────┐    LDAP Sync    ┌─────────────┐
│  │   Angular    │ ──────────────▶   │   Keycloak   │ ◀──────────────▶│   OpenLDAP  │
│  │   Frontend   │                   │    Realm     │                 │   / AD      │
│  │              │ ◀──────────────   │              │                 │             │
│  └──────────────┘    JWT Token      └──────────────┘                 └─────────────┘
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Architecture Decision: OIDC Authentication

### Decision
**Auth Flow:** Angular Frontend → OIDC (OpenID Connect) → Custom Keycloak Realm

### Context
OneBook requires a Zero-Trust, enterprise-grade authentication system that:
1. Supports SSO across all microservices
2. Integrates with existing corporate directories (LDAP/Active Directory)
3. Provides standardized JWT tokens with embedded roles
4. Allows centralized session management

### Rationale
- **OIDC/OAuth2:** Industry standard, widely supported, battle-tested
- **Keycloak:** Open-source, enterprise features, easy LDAP federation
- **Stateless JWT:** No server-side session storage, horizontal scaling

---

## User Federation Architecture

### Decision
**Keycloak will NOT store users locally.** All user data syncs from an external LDAP directory.

### Federation Flow
```
LDAP Directory (Source of Truth)
       │
       ▼ (Sync on login / periodic)
┌──────────────────────────────────────┐
│           Keycloak Realm             │
│  ┌──────────────────────────────┐    │
│  │   User Federation Provider   │    │
│  │   - Import Users: ON_DEMAND  │    │
│  │   - Sync Mode: READ_ONLY     │    │
│  │   - Edit Mode: UNSYNCED      │    │
│  └──────────────────────────────┘    │
└──────────────────────────────────────┘
       │
       ▼ (JWT Token)
   Angular App
```

### LDAP Group → Keycloak Role Mapping

| LDAP Group DN | Keycloak Role | Description |
|---------------|---------------|-------------|
| `cn=accountants,ou=groups,dc=onebook,dc=local` | `ROLE_ACCOUNTANT` | Standard accounting operations |
| `cn=admins,ou=groups,dc=onebook,dc=local` | `ROLE_ADMIN` | Full administrative access |
| `cn=auditors,ou=groups,dc=onebook,dc=local` | `ROLE_AUDITOR` | Read-only audit portal access |
| `cn=managers,ou=groups,dc=onebook,dc=local` | `ROLE_MANAGER` | Approval workflows |

### JWT Token Structure
```json
{
  "sub": "user-uuid",
  "preferred_username": "john.doe",
  "email": "john.doe@company.com",
  "realm_access": {
    "roles": ["ROLE_ACCOUNTANT", "ROLE_MANAGER"]
  },
  "resource_access": {
    "onebook-app": {
      "roles": ["view-reports", "post-entries"]
    }
  },
  "tenant_id": "tenant-uuid",
  "iss": "https://keycloak.onebook.local/realms/onebook",
  "exp": 1712058000
}
```

---

## Security Considerations

### Token Handling
- Access tokens stored in memory only (never localStorage for security)
- Refresh tokens stored in httpOnly cookies (when using BFF pattern)
- Token auto-refresh via `angular-oauth2-oidc` silent refresh

### CORS Configuration
```yaml
Keycloak Realm Settings:
  Web Origins: 
    - https://app.onebook.local
    - http://localhost:4200 (dev only)
```

### Multi-Tenancy Integration
The `tenant_id` claim in the JWT is extracted and used to:
1. Set PostgreSQL session variable: `SET app.current_tenant_id = '...'`
2. Enable Row-Level Security (RLS) filtering
3. Scope cache keys: `onebook:cache:{tenant_id}:{domain}:{id}`

---

## Implementation Stack

### Frontend (Angular 19+)
- **Library:** `angular-oauth2-oidc`
- **Configuration:** `environment.ts` → Keycloak realm endpoints
- **Guard:** `AuthGuard` → Protects routes requiring authentication
- **Interceptor:** `AuthInterceptor` → Injects Bearer token into API calls

### Backend (Spring Boot 3.4+)
- **Dependency:** `spring-boot-starter-oauth2-resource-server`
- **Configuration:** JWT validation against Keycloak JWKS endpoint
- **Roles Extraction:** Custom `JwtAuthenticationConverter`

### Infrastructure
- **Keycloak 24+** with custom realm (`onebook`)
- **OpenLDAP** or **Active Directory** for user federation
- **Redis** for session blacklist (logout invalidation)

---

## Configuration Reference

### Angular Environment (development)
```typescript
export const environment = {
  production: false,
  keycloak: {
    issuer: 'http://localhost:8180/realms/onebook',
    clientId: 'onebook-frontend',
    redirectUri: 'http://localhost:4200',
    scope: 'openid profile email roles',
    responseType: 'code',
    showDebugInformation: true
  }
};
```

### Keycloak Realm Export (key settings)
```json
{
  "realm": "onebook",
  "enabled": true,
  "sslRequired": "external",
  "registrationAllowed": false,
  "loginWithEmailAllowed": true,
  "userFederationProviders": [{
    "providerName": "ldap",
    "config": {
      "vendor": "other",
      "connectionUrl": "ldap://openldap:389",
      "usersDn": "ou=users,dc=onebook,dc=local",
      "bindDn": "cn=admin,dc=onebook,dc=local",
      "bindCredential": "${LDAP_BIND_PASSWORD}",
      "userObjectClasses": "inetOrgPerson",
      "syncRegistrations": "false",
      "importEnabled": "true"
    }
  }],
  "clients": [{
    "clientId": "onebook-frontend",
    "publicClient": true,
    "redirectUris": ["http://localhost:4200/*"],
    "webOrigins": ["http://localhost:4200"]
  }]
}
```

---

## Related Documentation

- `memory-bank/systempatterns.md` — Core architectural patterns
- `docs/technical/operational-runbook.md` — Deployment procedures
- `.github/agents/security-warden.md` — Security agent ownership

---

## Change Log

| Date | Change | Author |
|------|--------|--------|
| 2026-04-02 | Initial auth architecture documentation | AI Agent |

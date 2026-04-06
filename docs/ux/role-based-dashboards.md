# Role-Based Dashboards — Architecture & UX Specification

> **Status:** Draft — Engineering Reference  
> **Owner:** @UXSpecialist / @Architect  
> **Last Updated:** 2026-04-04

---

## 1. Purpose

This document is the **single source of truth** for OneBook's role-based, admin-configurable dashboard system. It captures all architectural decisions, data models, runtime rules, Admin Template Editor rules, add-ons zone behaviour, accessibility requirements, and REST/caching guidance.

Engineering teams MUST consult this document before implementing any dashboard-related feature.

---

## 2. Terminology

| Term | Definition |
|------|-----------|
| **Role Policy** | The complete admin-defined configuration for one role (e.g., `ACCOUNTING_HEAD`). Contains all workspace templates and add-ons zone settings for that role. Identified by `(tenantId, role)`. Has a monotonically increasing `policyVersion`. |
| **Workspace** | A named tab within a role's dashboard (e.g., "Cash Overview", "Payables"). Each workspace has its own 12-column grid layout. |
| **Workspace Template** | The admin-defined card layout for one workspace. Contains an ordered list of `CardLayout` entries and an optional `addonsZone`. |
| **Add-ons Zone** | An optional, designated grid region available to certain roles. Users may add/remove extra module cards here without admin intervention. Add-ons are per-workspace. |
| **Lock Level** | Per-card setting controlling whether a card can be moved in the Admin Template Editor. Values: `locked` (never movable), `none` (movable by admin editor). |
| **Runtime** | The live production dashboard presented to end-users. Runtime dashboards are always non-draggable. |
| **Admin Template Editor** | The admin-only interface for designing workspace layouts. The only place where card positions are changed. Uses move-only (no resize). |
| **ETag** | HTTP entity tag used for optimistic concurrency on policy GET/PUT. |
| **SWR** | Stale-while-revalidate client caching pattern. |
| **SSE** | Server-Sent Events — used for policy invalidation notifications only (data is never streamed). |
| **policyVersion** | Monotonically increasing integer scoped to `(tenantId, role)`. Incremented on every workspace template publish. |
| **templateVersion** | Per-workspace version counter. Incremented when that workspace's template is published. |

---

## 3. Roles

| Role Constant | Display Name | Workspaces | Add-ons Zone |
|---------------|-------------|-----------|-------------|
| `OWNER_MD` | Owner / Managing Director | Admin-configured | No |
| `ACCOUNTING_HEAD` | Accounting Head | Admin-configured | No |
| `ACCOUNTANT` | Accountant | Admin-configured | Yes |
| `CLERK` | Clerk | Admin-configured | Yes |
| `ADMIN` | System Administrator | Admin-configured | No |

> Roles beyond `ADMIN` (e.g., `AUDITOR`) inherit the existing `ROLE_AUDITOR` guard and do not use the role-based dashboard system.

---

## 4. Data Models

### 4.1 TypeScript Interfaces (canonical source: `frontend/src/app/dashboard/models/dashboard-layout.models.ts`)

```typescript
/** Roles that participate in the role-based dashboard system. */
export type DashboardRole =
  | 'OWNER_MD'
  | 'ACCOUNTING_HEAD'
  | 'ACCOUNTANT'
  | 'CLERK'
  | 'ADMIN';

/**
 * Lock level for a card in the Admin Template Editor.
 * - 'locked': card cannot be moved or removed by any admin editor action.
 * - 'none':   card is freely movable by the admin editor.
 */
export type CardLockLevel = 'locked' | 'none';

/** 12-column grid position for a card. All values are 1-indexed. */
export interface GridRect {
  col: number;  // 1–12
  row: number;  // 1-based row
  w: number;    // column span (1–12)
  h: number;    // row span (in grid row units)
}

/** Layout entry for a single card within a workspace. */
export interface CardLayout {
  cardId: string;
  moduleKey: string;
  rect: GridRect;
  lock: CardLockLevel;
}

/** The add-ons zone definition for a workspace. */
export interface AddonsZoneDef {
  rect: GridRect;           // reserved area on the grid
  maxCards: number;         // max additional module cards
  allowedModuleKeys: string[]; // which modules can be placed here
}

/** A single workspace template (one tab in the dashboard). */
export interface WorkspaceTemplate {
  workspaceId: string;
  label: string;
  icon?: string;
  cards: CardLayout[];
  addonsZone?: AddonsZoneDef;  // present only for roles that allow add-ons
  templateVersion: number;
  publishedAtIso: string;
  publishedBy: string;
}

/** Add-on card placed by a user in the add-ons zone. */
export interface AddonCard {
  cardId: string;
  moduleKey: string;
  rect: GridRect;  // determined by auto-placement; user cannot drag
}

/** Per-user add-ons preferences for one workspace. */
export interface UserAddonsPrefs {
  tenantId: string;
  userId: string;
  role: DashboardRole;
  workspaceId: string;
  addons: AddonCard[];
}

/** The complete role policy served by GET /api/tenants/:tenantId/roles/:role/policy. */
export interface RoleDashboardPolicy {
  tenantId: string;
  role: DashboardRole;
  policyVersion: number;
  workspaces: WorkspaceTemplate[];
  updatedAtIso: string;
}

/** Safe user preferences stored in runtime (never includes card positions). */
export interface UserDashboardPrefs {
  tenantId: string;
  userId: string;
  role: DashboardRole;
  lastWorkspaceId?: string;
  densityOverride?: 'compact' | 'comfortable' | 'spacious';  // only if role allows
}

/** Cached policy entry stored in IndexedDB. */
export interface CachedRolePolicy {
  etag: string;
  policy: RoleDashboardPolicy;
  cachedAtIso: string;
}
```

---

## 5. Runtime Behaviour Rules

### 5.1 Locked dashboards
- All card positions in production runtime come **exclusively** from the published `WorkspaceTemplate`.
- Runtime users **cannot** drag, resize, add, or remove cards from the locked card region.
- The layout is rendered as a static 12-column grid — no drag library is loaded on runtime routes.

### 5.2 Workspaces as tabs
- Each role has 1–N workspaces, presented as horizontal tabs.
- Default active workspace is `UserDashboardPrefs.lastWorkspaceId` (if valid); otherwise the first workspace in the policy.
- Tab navigation: `Tab` / `Shift+Tab` cycles between workspaces; `Enter` activates focused tab.
- On mobile/small viewport: tabs collapse to a dropdown menu (ARIA `combobox`).

### 5.3 Density
- Roles `OWNER_MD` and `ACCOUNTING_HEAD` may have a `densityOverride` preference stored in `UserDashboardPrefs`.
- Density is a CSS class applied to the workspace grid: `density-compact`, `density-comfortable` (default), `density-spacious`.
- Clerks and Accountants inherit admin-configured density; they cannot override it.

### 5.4 Startup sequence (SWR)
1. Load cached policy from IndexedDB instantly → render locked dashboard.
2. In background, call `GET /api/tenants/:tenantId/roles/:role/policy` with `If-None-Match: <cachedEtag>`.
3. On `304`: no-op.
4. On `200`: update IndexedDB cache + re-render.
5. Subscribe to SSE invalidations → trigger step 2 on `role_policy_updated` event.

---

## 6. Admin Template Editor Rules

### 6.1 Access
- The Admin Template Editor is available only to users with `ROLE_ADMIN`.
- Route: `GET /admin/dashboard-templates/:role/:workspaceId` (client-side only; no server-side render).
- Guarded by `adminGuard`.

### 6.2 Move-only (no resize)
- Admins can only **move** cards — no resize handles are exposed.
- Card dimensions (`w`, `h`) are set at card registration time and cannot be changed in the editor.

### 6.3 Movable cards
- Only cards with `lock = 'none'` can be moved.
- Cards with `lock = 'locked'` are visually pinned (shown with a lock icon overlay).

### 6.4 Grid
- 12-column grid with pixel-snapping to column boundaries.
- Rows are auto-height based on card content, or a configurable fixed row height in the template.

### 6.5 Overlap detection and revert
- Before accepting a drag drop, the editor checks for overlapping `GridRect` pairs.
- If overlap detected: the card snaps back to its previous position (with a brief animation) and a toast notification is shown: "Cards cannot overlap."

### 6.6 Publish validation
- Before calling PUT, the editor validates:
  1. All card `rect` values are within bounds (col + w ≤ 13; row ≥ 1).
  2. No two cards overlap.
  3. The `addonsZone.rect` (if present) does not overlap with any locked card.
- If validation fails: publish is blocked and errors are shown inline.

### 6.7 Conflict handling (`412 Precondition Failed`)
When the server returns `412`:
- Show modal: "Template changed since you started editing."
- Display absolute timestamps (ISO, converted to local timezone):
  - "You started editing at `<startedAtIso>`"
  - "Latest template published at `<serverPublishedAtIso>`"
- Options:
  1. **Reload latest** — discard current draft.
  2. **Compare** — show diff: moved cards, added/removed add-ons.
  3. **Re-apply my add-ons changes** — replay only add-ons zone mutations onto the latest template (safe because add-ons zone is isolated).

---

## 7. Add-ons Zone Rules

### 7.1 Eligibility
- Add-ons zones exist **only** for roles `ACCOUNTANT` and `CLERK`.
- An add-ons zone must be explicitly defined in the workspace template (`addonsZone != null`).
- If `addonsZone` is absent for a workspace, users see no add-ons capability.

### 7.2 User actions
- Users can **add** a module card from a curated list (`allowedModuleKeys`).
- Users can **remove** their own add-on cards.
- Users **cannot** drag add-on cards to arbitrary positions.

### 7.3 Auto-placement algorithm
When a user adds a module card to the add-ons zone:
1. Collect all currently placed `AddonCard` entries for this workspace.
2. Sort by `rect.row` ascending, then `rect.col` ascending.
3. Find the first available slot within `addonsZone.rect` that accommodates the new card's dimensions (default `w=3, h=2`).
4. A "slot" is available if it does not overlap with any existing `AddonCard.rect`.
5. Scan left-to-right, top-to-bottom within the zone's bounding box.
6. If no slot found (zone full): show error "Add-ons zone is full (max `<maxCards>` cards)."
7. Assign the found slot as `AddonCard.rect` and save.

### 7.4 Runtime vs editor
- In **runtime**: add-on cards are rendered at their persisted `rect` (fixed, no drag).
- In **Admin Template Editor**: add-on card positions within the zone are movable (subject to `lock = 'none'` rule).

### 7.5 Persistence
- Add-on preferences are stored per `(tenantId, userId, role, workspaceId)`.
- Server is source of truth; client uses SWR caching.
- Add-on positions are **user prefs**, not part of the published `WorkspaceTemplate`.

---

## 8. Accessibility & Keyboard Behaviour

### 8.1 Workspace tab strip
- Tab strip uses ARIA `role="tablist"` / `role="tab"` / `role="tabpanel"`.
- `Tab` / `Shift+Tab`: move browser focus between tabs (roving `tabindex`).
- `ArrowLeft` / `ArrowRight`: cycle active tab.
- `Enter` / `Space`: activate focused tab.
- `Home` / `End`: jump to first / last tab.

### 8.2 Admin Template Editor
- Keyboard-movable cards: focused card responds to `ArrowUp/Down/Left/Right` (1 column/row per keypress, `Shift+Arrow` for 3 at a time).
- `Escape`: cancel drag / revert card to last saved position.
- `Enter` on a card: open card settings (future).
- Editor toolbar buttons must be keyboard-reachable with visible focus indicators.

### 8.3 Add-ons zone
- Add/Remove controls are standard buttons with accessible labels.
- Module picker uses ARIA `listbox` with keyboard navigation.

### 8.4 General
- All interactive elements meet WCAG 2.1 AA contrast requirements.
- Focus order follows DOM order (no `tabindex > 0`).
- Error messages are announced via ARIA `live="assertive"`.
- Loading states use ARIA `aria-busy="true"` on the workspace container.

---

## 9. REST API & Caching Guidance

### 9.1 Endpoints

#### Get role policy (all workspaces)
```
GET /api/tenants/:tenantId/roles/:role/policy
```
- Response: `200 OK` — body: `RoleDashboardPolicy`
- Response headers:
  - `ETag: "<policy-etag>"`
  - `Cache-Control: private, max-age=0`
- Conditional GET: `If-None-Match: "<last-etag>"` → `304 Not Modified` (no body) or `200` with new body + new ETag.

#### Publish one workspace template
```
PUT /api/tenants/:tenantId/roles/:role/workspaces/:workspaceId/template
```
- Request body: `{ workspaceId, cards, addonsZone?, updatedAtIso }`
- Required request header: `If-Match: "<policy-etag>"`
- `200 OK`: `{ policyVersion, workspace }` + new `ETag` header
- `412 Precondition Failed`: conflict payload:
  ```json
  {
    "error": "PRECONDITION_FAILED",
    "message": "Policy has been updated since your session started.",
    "serverPolicyVersion": 18,
    "serverPublishedAtIso": "2026-04-04T10:19:00Z"
  }
  ```

#### Save user add-ons prefs (per workspace)
```
PUT /api/tenants/:tenantId/users/:userId/dashboard-prefs/:workspaceId/addons
```
- Request body: `UserAddonsPrefs`
- `200 OK` — persisted prefs returned.

#### Save user dashboard prefs (density, last workspace)
```
PUT /api/tenants/:tenantId/users/:userId/dashboard-prefs
```
- Request body: `UserDashboardPrefs`
- `200 OK`.

### 9.2 SSE invalidation
```
GET /api/tenants/:tenantId/events   (SSE stream)
```
- Events emitted:
  - `event: role_policy_updated` / `data: { "role": "ACCOUNTANT", "policyVersion": 18 }`
- Client reaction:
  - If `event.role` matches the current user's role: call policy GET with `If-None-Match`.
  - On `304`: no-op.
  - On `200`: update store and re-render.

### 9.3 Polling fallback
- When SSE is unavailable (connection error, network proxy, etc.):
  - Runtime users: poll every **120 seconds** with conditional GET.
  - Admin editor open: poll every **30 seconds** with conditional GET.
- All polls use `If-None-Match`; most will return `304` (zero payload cost).

### 9.4 Client-side cache (IndexedDB keys)
| Key | Value |
|-----|-------|
| `rolePolicy:{tenantId}:{role}` | `CachedRolePolicy` (etag + policy + cachedAt) |
| `userPrefs:{tenantId}:{userId}:{role}` | `UserDashboardPrefs` |
| `userAddons:{tenantId}:{userId}:{role}:{workspaceId}` | `UserAddonsPrefs` |

### 9.5 ETag flow summary
```
Client                              Server
  │── GET /policy ──────────────────►│
  │◄─ 200 + ETag: "v17:abc" ────────│
  │   (store ETag in signal + IDB)   │
  │                                  │
  │── GET /policy                    │
  │   If-None-Match: "v17:abc" ─────►│
  │◄─ 304 Not Modified ─────────────│  (fast path)
  │                                  │
  │── PUT /workspaces/:id/template   │
  │   If-Match: "v17:abc" ──────────►│
  │◄─ 200 + ETag: "v18:xyz" ────────│  (success)
  │                                  │
  │── PUT /workspaces/:id/template   │
  │   If-Match: "v17:abc" ──────────►│  (stale!)
  │◄─ 412 Precondition Failed ───────│
```

### 9.6 Single-domain development note
During development (no purchased domain yet), the app runs on a single origin (`localhost`). The `authInterceptor` attaches `Authorization: Bearer` to all relative `/api/` requests. No cross-origin configuration is required until a dedicated API domain is provisioned. When a separate API domain is added, update `shouldSkipAuth` in `auth.interceptor.ts` to also allow that origin.

---

## 10. Non-Goals

The following are **explicitly out of scope** for this specification:

- ❌ Runtime drag-and-drop (cards are locked at runtime without exception).
- ❌ Freeform / user-customisable layout mode (no "personalise my dashboard" for end users).
- ❌ Card resize in the Admin Template Editor (move-only).
- ❌ Per-user card addition outside the add-ons zone.
- ❌ Multi-region SSE fan-out (single-endpoint approach is used; revisit when multi-region routing is added).
- ❌ Explicit "home region" header (`X-Tenant-Home-Region`) — deferred until multi-region routing is needed.
- ❌ Drag-from-library in add-ons zone (auto-placement only).
- ❌ Dashboard theme customisation per workspace (future consideration).

---

## 11. Implementation Checklist

Use this as the acceptance-criteria checklist when implementing:

- [ ] `RoleDashboardPolicy` API endpoint returns `ETag` header.
- [ ] Conditional GET (`If-None-Match`) returns `304` on match.
- [ ] Publish PUT requires `If-Match`; returns `412` with conflict payload on mismatch.
- [ ] SSE endpoint emits `role_policy_updated` events.
- [ ] Angular `RolePolicyStore` captures `ETag` from GET response headers.
- [ ] Angular `RolePolicyStore` sends `If-Match` on publish PUT.
- [ ] Angular `RolePolicyStore` exposes `publishConflict` signal for `412` handling.
- [ ] `WorkspaceShellComponent` renders locked 12-column grid (no drag library imported).
- [ ] `AdminTemplateEditorShellComponent` allows move-only for `lock='none'` cards.
- [ ] Overlap detection reverts card position on conflict.
- [ ] Add-ons zone auto-placement algorithm implemented.
- [ ] Tab strip uses correct ARIA roles and keyboard navigation.
- [ ] IndexedDB SWR cache implemented for startup performance.
- [ ] Polling fallback activates when SSE connection fails.
- [ ] Admin conflict modal displays absolute timestamps and 3 resolution options.

---

## 12. Related Documents

- [Architecture Overview](../architecture.md)
- [API Documentation](../technical/api-documentation.md)
- [Key-Binding Registry](../technical/key-binding-registry.md)
- [Developer Guide](../technical/developer-guide.md)
- [Frontend Model: `dashboard-layout.models.ts`](../../frontend/src/app/dashboard/models/dashboard-layout.models.ts)

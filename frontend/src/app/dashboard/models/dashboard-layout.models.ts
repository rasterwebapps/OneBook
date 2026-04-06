/**
 * Dashboard Layout Models
 *
 * Canonical TypeScript interfaces for OneBook's role-based, admin-configurable
 * dashboard system.
 *
 * Spec: docs/ux/role-based-dashboards.md
 */

// ---------------------------------------------------------------------------
// Roles
// ---------------------------------------------------------------------------

/** Roles that participate in the role-based dashboard system. */
export type DashboardRole =
  | 'OWNER_MD'
  | 'ACCOUNTING_HEAD'
  | 'ACCOUNTANT'
  | 'CLERK'
  | 'ADMIN';

// ---------------------------------------------------------------------------
// Grid
// ---------------------------------------------------------------------------

/**
 * Position and dimensions of a card on the 12-column grid.
 * All values are 1-indexed.
 */
export interface GridRect {
  /** Starting column (1–12). */
  col: number;
  /** Starting row (1-based). */
  row: number;
  /** Column span (1–12). col + w must be ≤ 13. */
  w: number;
  /** Row span (in grid row units). */
  h: number;
}

// ---------------------------------------------------------------------------
// Cards & Lock Levels
// ---------------------------------------------------------------------------

/**
 * Lock level for a card in the Admin Template Editor.
 * - 'locked': card is pinned; cannot be moved or removed by any editor action.
 * - 'none':   card is movable by the admin editor.
 */
export type CardLockLevel = 'locked' | 'none';

/** Layout entry for a single card within a workspace. */
export interface CardLayout {
  /** Stable identifier for this card instance within the workspace. */
  cardId: string;
  /** Key of the module rendered by this card (e.g., 'cash-flow-summary'). */
  moduleKey: string;
  /** Position and size on the 12-column grid. */
  rect: GridRect;
  /** Whether this card can be moved in the Admin Template Editor. */
  lock: CardLockLevel;
}

// ---------------------------------------------------------------------------
// Add-ons Zone
// ---------------------------------------------------------------------------

/** Definition of the add-ons zone for a workspace. */
export interface AddonsZoneDef {
  /** Reserved area on the 12-column grid for add-on cards. */
  rect: GridRect;
  /** Maximum number of add-on cards allowed in this zone. */
  maxCards: number;
  /** Module keys that users may add to this zone. */
  allowedModuleKeys: string[];
}

/** A user-placed add-on card within the add-ons zone. */
export interface AddonCard {
  /** Stable identifier for this add-on card instance. */
  cardId: string;
  /** Module key rendered by this add-on card. */
  moduleKey: string;
  /**
   * Grid position determined by auto-placement algorithm.
   * Users cannot drag add-on cards to arbitrary positions.
   */
  rect: GridRect;
}

/** Per-user add-on preferences for one workspace. */
export interface UserAddonsPrefs {
  tenantId: string;
  userId: string;
  role: DashboardRole;
  workspaceId: string;
  addons: AddonCard[];
}

// ---------------------------------------------------------------------------
// Workspace Template
// ---------------------------------------------------------------------------

/** Admin-published layout for a single workspace (one tab in the dashboard). */
export interface WorkspaceTemplate {
  /** Stable workspace identifier. */
  workspaceId: string;
  /** Display label shown on the tab. */
  label: string;
  /** Optional icon key or emoji for the tab. */
  icon?: string;
  /** Ordered list of card layouts for the locked grid region. */
  cards: CardLayout[];
  /**
   * Add-ons zone definition.
   * Present only for roles ACCOUNTANT and CLERK, and only when the admin
   * has explicitly configured an add-ons zone for this workspace.
   */
  addonsZone?: AddonsZoneDef;
  /** Incremented each time this workspace's template is published. */
  templateVersion: number;
  /** ISO-8601 timestamp of the last publish. */
  publishedAtIso: string;
  /** Username or ID of the admin who last published this template. */
  publishedBy: string;
}

// ---------------------------------------------------------------------------
// Role Dashboard Policy
// ---------------------------------------------------------------------------

/**
 * Complete role policy served by:
 *   GET /api/tenants/:tenantId/roles/:role/policy
 *
 * The server includes an ETag response header for conditional GET / optimistic
 * concurrency on publish. See docs/ux/role-based-dashboards.md §9.
 */
export interface RoleDashboardPolicy {
  tenantId: string;
  role: DashboardRole;
  /** Monotonically increasing version; incremented on every workspace template publish. */
  policyVersion: number;
  workspaces: WorkspaceTemplate[];
  /** ISO-8601 timestamp of the last policy change. */
  updatedAtIso: string;
}

// ---------------------------------------------------------------------------
// User Preferences (safe prefs — never includes card positions)
// ---------------------------------------------------------------------------

/**
 * Safe user preferences stored in runtime.
 * Card positions are NEVER stored here — they always come from WorkspaceTemplate.
 */
export interface UserDashboardPrefs {
  tenantId: string;
  userId: string;
  role: DashboardRole;
  /** ID of the last workspace the user had active; used to restore tab on next login. */
  lastWorkspaceId?: string;
  /**
   * Density override — only allowed for OWNER_MD and ACCOUNTING_HEAD.
   * Clerks and Accountants use admin-configured density.
   */
  densityOverride?: 'compact' | 'comfortable' | 'spacious';
}

// ---------------------------------------------------------------------------
// Client-Side SWR Cache
// ---------------------------------------------------------------------------

/** Entry stored in IndexedDB for stale-while-revalidate caching. */
export interface CachedRolePolicy {
  /** Last ETag received from the server for this policy. Used for If-None-Match. */
  etag: string;
  policy: RoleDashboardPolicy;
  /** ISO-8601 timestamp of when this entry was cached. */
  cachedAtIso: string;
}

// ---------------------------------------------------------------------------
// Publish Conflict (412 Precondition Failed)
// ---------------------------------------------------------------------------

/** Payload returned by the server on 412 Precondition Failed during template publish. */
export interface PublishConflictDetail {
  error: 'PRECONDITION_FAILED';
  message: string;
  serverPolicyVersion: number;
  serverPublishedAtIso: string;
}

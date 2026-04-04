# Keyboard Shortcuts Reference
## OneBook — Nexus Universal Accounting OS

> **Complete keyboard shortcut reference for power users.**  
> OneBook achieves Tally-equivalent keyboard speed via F-key shortcuts and Command Palette.  
> Last Updated: 2026-03-18 | Source: `key-binding-registry.service.ts`, `docs/technical/key-binding-registry.md`

---

## Quick Reference Card

| Shortcut | Action |
|----------|--------|
| `Ctrl+K` / `Cmd+K` | **Command Palette** — open from anywhere |
| `F4` | Contra Voucher |
| `F5` | Payment Voucher |
| `F6` | Receipt Voucher |
| `F7` | Journal Voucher |
| `F8` | Sales Voucher |
| `F9` | Purchase Voucher |
| `Ctrl+A` | Accept / Save |
| `Escape` | Cancel / Close |
| `Alt+C` | Create new account |

---

## 1. Command Palette

The Command Palette (`Ctrl+K`) is the single most powerful shortcut — it provides access to every screen and action.

| Shortcut | Action |
|----------|--------|
| `Ctrl+K` | Open Command Palette (Windows/Linux) |
| `Cmd+K` | Open Command Palette (Mac) |
| `↑` `↓` | Navigate suggestions |
| `Enter` | Select highlighted option |
| `Escape` | Close Command Palette |
| Type any text | Filter suggestions in real-time |

**Example searches in Command Palette:**
```
"payment"     → Payment Voucher (F5)
"trial"       → Trial Balance Report
"balance"     → Balance Sheet Report
"chart"       → Chart of Accounts
"forecast"    → AI Cash Flow Forecast
"reconcile"   → Bank Reconciliation
"tds"         → TDS Register
"fixed"       → Fixed Asset Register
"anomaly"     → Anomaly Alerts
"audit"       → Auditor Portal
```

---

## 2. Tally Legacy Shortcuts (Voucher Entry)

These shortcuts replicate the Tally ERP experience for trained accountants.

### Voucher Type Selection (Global)
| Shortcut | Voucher Type | Use Case |
|----------|-------------|---------|
| `F4` | Contra Voucher | Cash ↔ Bank transfers |
| `F5` | Payment Voucher | Payments going out |
| `F6` | Receipt Voucher | Receipts coming in |
| `F7` | Journal Voucher | Adjusting/general entries |
| `F8` | Sales Voucher | Revenue transactions |
| `F9` | Purchase Voucher | Purchase transactions |

### Entry Acceptance
| Shortcut | Action |
|----------|--------|
| `Ctrl+A` | Accept / Save the current entry |
| `Enter` | Move to next field / Accept current field |
| `Escape` | Cancel current entry / Go back |
| `Ctrl+Z` | Undo last field change |

### Account Picker (Within Voucher)
| Shortcut | Action |
|----------|--------|
| `Alt+C` | Create a new account (opens account creation dialog) |
| `Type + Enter` | Fuzzy search accounts by name or code |
| `↑` `↓` | Navigate account suggestions |
| `Tab` | Accept selected account, move to amount field |

### Line Entry Navigation
| Shortcut | Action |
|----------|--------|
| `Tab` | Move to next field in line |
| `Shift+Tab` | Move to previous field |
| `Enter` (on amount) | Accept line, add new line |
| `Alt+D` | Delete current line |
| `Insert` | Insert new line above current |

---

## 3. Navigation Shortcuts

### Global Navigation
| Shortcut | Action |
|----------|--------|
| `Ctrl+K` | Open Command Palette (fastest navigation) |
| `Ctrl+Home` | Go to Dashboard |
| `Alt+←` | Navigate back (browser history) |
| `Alt+→` | Navigate forward (browser history) |
| `Ctrl+R` | Refresh current page/report |
| `Ctrl+Shift+R` | Hard refresh (clear cache + reload) |

### Module Navigation
| Shortcut | Destination |
|----------|------------|
| `Ctrl+K` → "accounts" | Chart of Accounts |
| `Ctrl+K` → "voucher" | Voucher Entry |
| `Ctrl+K` → "reports" | Reports Dashboard |
| `Ctrl+K` → "banking" | Bank Reconciliation |
| `Ctrl+K` → "gst" | GST/Compliance module |
| `Ctrl+K` → "assets" | Fixed Asset Register |
| `Ctrl+K` → "ai" | AI Intelligence Dashboard |
| `Ctrl+K` → "auditor" | Auditor Portal |

---

## 4. Voucher List & Transaction Shortcuts

### Transaction List Navigation
| Shortcut | Action |
|----------|--------|
| `↑` `↓` | Move selection up/down in list |
| `Enter` | Open selected transaction |
| `Ctrl+F` | Open search/filter panel |
| `Ctrl+E` | Export visible list |
| `Page Up` | Previous page |
| `Page Down` | Next page |

### Transaction Actions (when viewing a transaction)
| Shortcut | Action |
|----------|--------|
| `Ctrl+P` | Print voucher |
| `Ctrl+Shift+R` | Create reversal entry |
| `E` | Edit (if in DRAFT status) |
| `Escape` | Close and return to list |

---

## 5. Report Shortcuts

### Report Generation
| Shortcut | Action |
|----------|--------|
| `Enter` | Generate report with current parameters |
| `Ctrl+R` | Refresh / Re-generate report |
| `←` | Previous period (e.g., previous month) |
| `→` | Next period |
| `Ctrl+Shift+←` | Previous year |
| `Ctrl+Shift+→` | Next year |

### Report Export
| Shortcut | Action |
|----------|--------|
| `Ctrl+E` | Export current report (opens format dialog) |
| `Ctrl+Shift+P` | Export as PDF (direct) |
| `Ctrl+Shift+X` | Export as Excel (direct) |
| `Ctrl+Shift+C` | Export as CSV (direct) |
| `Ctrl+P` | Print current report |

### Report Drill-Down
| Shortcut | Action |
|----------|--------|
| `Enter` (on account row) | Drill into account details |
| `Escape` | Return to parent report |
| `Space` | Expand/collapse row in tree reports |

---

## 6. Auditor Portal Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+K` → "verify" | Open hash chain verification |
| `↑` `↓` | Navigate audit entries |
| `Enter` | View entry details |
| `V` | Verify hash chain for selected entity |

---

## 7. Maker-Checker Workflow Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+K` → "pending" | View pending approvals |
| `A` | Approve selected item (on workflow list) |
| `R` | Reject selected item (opens rejection reason dialog) |
| `Enter` | Open selected workflow item |

---

## 8. Form Field Shortcuts

| Shortcut | Action |
|----------|--------|
| `Tab` | Next field |
| `Shift+Tab` | Previous field |
| `Enter` | Accept field value |
| `Escape` | Clear field / Cancel |
| `Ctrl+A` | Select all text in field |
| `Ctrl+C` | Copy selected text |
| `Ctrl+V` | Paste |
| Type in date field | Use format DDMMYYYY (auto-formats) |
| Type in amount field | Use digits and decimal point only |

---

## 9. Date Entry Quick Keys

OneBook supports Tally-style quick date entry in date fields:

| Input | Result |
|-------|--------|
| `T` | Today |
| `Y` | Yesterday |
| `+` | Tomorrow |
| `+N` | N days from today (e.g., `+7` = 7 days from now) |
| `-N` | N days ago (e.g., `-7` = 7 days ago) |
| `DDMMYYYY` | Full date (e.g., 15012026 = 15 Jan 2026) |
| `DDMM` | Day+Month of current year |

---

## 10. Amount Entry Tips

- Use period `.` as decimal separator: `5000.50`
- Commas are ignored: `1,00,000` = `100000`
- Use `K` for thousands: `50K` = `50000`
- Use `L` for lakhs: `5L` = `500000`
- Use `C` for crores: `1C` = `10000000`

---

## 11. Browser-Level Shortcuts (Standard)

These are standard browser shortcuts that work within OneBook:

| Shortcut | Action |
|----------|--------|
| `F5` | Refresh page (may conflict with Receipt Voucher in voucher screen) |
| `Ctrl+L` | Focus browser address bar |
| `Ctrl+T` | New browser tab |
| `Ctrl+W` | Close browser tab |
| `Ctrl++` | Zoom in |
| `Ctrl+-` | Zoom out |
| `Ctrl+0` | Reset zoom |

> **Note:** In voucher entry screens, F-key shortcuts (`F4`–`F9`) are captured by OneBook and do not trigger browser actions.

---

## Shortcut Customization

OneBook's keyboard shortcuts are registered in `KeyBindingRegistry` and can be extended by IT Administrators. See `docs/technical/key-binding-registry.md` for the full specification.

**Registry service:** `key-binding-registry.service.ts`  
**Configuration:** Keyboard shortcuts are defined per screen context (contextual power keys adapt to the current screen).

---

## Accessibility

- All keyboard shortcuts have equivalent mouse/touch alternatives
- Screen readers are supported for all form fields
- High-contrast mode available in Settings
- Keyboard focus is always visible (outline on focused elements)

---

*For the full keyboard binding specification, see `docs/technical/key-binding-registry.md`.*

# User Manual
## OneBook — Nexus Universal Accounting OS

> **Complete guide for accountants, finance managers, and power users.**  
> Version: 1.0 | Last Updated: 2026-03-18

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Chart of Accounts](#2-chart-of-accounts)
3. [Journal Entry & Voucher Posting](#3-journal-entry--voucher-posting)
4. [Trial Balance and Reports](#4-trial-balance-and-reports)
5. [Bank Reconciliation](#5-bank-reconciliation)
6. [Fixed Assets](#6-fixed-assets)
7. [GST Compliance (TDS, TCS, e-Invoice)](#7-gst-compliance-tds-tcs-e-invoice)
8. [AI Dashboard](#8-ai-dashboard)
9. [Command Palette](#9-command-palette)
10. [Keyboard Shortcuts Reference](#10-keyboard-shortcuts-reference)
11. [Troubleshooting](#11-troubleshooting)

---

## 1. Getting Started

### 1.1 Login
Navigate to your OneBook instance (e.g., `https://onebook.yourcompany.com`). Enter your credentials. You will be redirected to the Dashboard after authentication.

**On successful login:**
- Your session token is issued (valid for 8 hours)
- Frequently accessed accounts are pre-loaded into the warm cache (this is why the app feels instant)
- Your tenant's chart of accounts and configuration are available immediately

### 1.2 Dashboard Overview
The main dashboard provides:
- **Quick Actions:** Post voucher, view reports, check alerts
- **AI Summary:** Cash position, anomaly count, forecast snapshot
- **Recent Transactions:** Last 10 posted vouchers
- **Pending Approvals:** Items awaiting your action in the Maker-Checker workflow

### 1.3 Navigation
Use the left sidebar to navigate between modules:
- `📒 Accounting` — Voucher entry, Chart of Accounts
- `📊 Reports` — Trial Balance, P&L, Balance Sheet, Cash Flow
- `🏦 Banking` — Bank Reconciliation
- `🏭 Inventory` — Stock management (if enabled)
- `🧾 GST` — TDS, TCS, e-Invoice, e-Way Bill
- `🤖 AI` — Forecasting, Anomaly Detection
- `📋 Auditor` — Audit trail, Workflow approvals

### 1.4 Command Palette
Press **Ctrl+K** (Windows/Linux) or **Cmd+K** (Mac) from anywhere in the app to open the Command Palette. Type to search for any screen, action, or voucher type. See [Section 9](#9-command-palette) for full details.

---

## 2. Chart of Accounts

### 2.1 Viewing the Chart of Accounts
Navigate to **Accounting → Chart of Accounts** or press `Ctrl+K` and type "Chart of Accounts".

The screen shows accounts in a tree hierarchy. Click on an account group to expand/collapse. Each account shows:
- Account Code
- Account Name
- Account Type (colour-coded: green=Assets, red=Liabilities, blue=Income, orange=Expenses)
- Current Balance

### 2.2 Creating a New Account
1. Click **+ New Account** or press `Alt+C`
2. Fill in the fields:
   - **Account Code:** Unique code (e.g., 1001, CASH-001)
   - **Account Name:** Display name
   - **Account Type:** Select from ASSET, LIABILITY, INCOME, EXPENSE, EQUITY
   - **Parent Account:** (Optional) Select parent for hierarchical grouping
   - **Opening Balance:** (Optional) Opening balance as of migration date
3. Press **Ctrl+A** or click **Save**

### 2.3 Account Groups (Default Chart of Accounts)
OneBook comes pre-seeded with a standard Indian chart of accounts:

| Group | Type | Examples |
|-------|------|---------|
| Current Assets | ASSET | Cash in Hand, Bank Account, Accounts Receivable |
| Fixed Assets | ASSET | Plant & Machinery, Furniture, Computers |
| Current Liabilities | LIABILITY | Accounts Payable, TDS Payable, GST Payable |
| Long-term Liabilities | LIABILITY | Term Loans, Debentures |
| Capital | EQUITY | Share Capital, Retained Earnings |
| Direct Income | INCOME | Sales Revenue, Service Revenue |
| Indirect Income | INCOME | Interest Received, Other Income |
| Direct Expenses | EXPENSE | Cost of Goods Sold, Raw Materials |
| Indirect Expenses | EXPENSE | Salaries, Rent, Utilities, Depreciation |

### 2.4 Editing an Account
Click on an account name to open its details panel. Click **Edit** or press `E`. Modify and press **Ctrl+A** to save.

> **Note:** Accounts with existing transactions cannot have their account type changed. Create a new account and use a journal entry to transfer the balance.

### 2.5 Account Hierarchy
Accounts support unlimited nesting. Subsidiary accounts roll up to their parent for reporting. To move an account, edit it and change the parent.

---

## 3. Journal Entry & Voucher Posting

### 3.1 Opening the Voucher Entry Screen
Use keyboard shortcuts (Tally-compatible):
- **F4** → Contra Entry
- **F5** → Payment Voucher
- **F6** → Receipt Voucher
- **F7** → Journal Voucher
- **F8** → Sales Voucher
- **F9** → Purchase Voucher

Or navigate to **Accounting → Voucher Entry** and select the voucher type.

### 3.2 Entering a Payment Voucher (F5)
1. Press **F5** to open Payment Voucher
2. **Date:** Defaults to today. Change if needed.
3. **Narration:** Description of the payment (encrypted before storage)
4. **Reference No.:** Cheque number, UTR, etc.
5. **Journal Entries:** Add lines:
   - Click **+ Add Line** or press `Tab` after the last line
   - Select **Account** (type to search)
   - Select **Type:** DEBIT or CREDIT
   - Enter **Amount** (BigDecimal — always use exact amounts)
   - Optionally assign a **Cost Center**
6. Verify: The ∑ Debit and ∑ Credit totals must match
7. Press **Ctrl+A** to post

> **Keyboard Navigation:** `Tab` moves between fields; `Enter` accepts; `Escape` cancels.

### 3.3 Understanding Double-Entry
Every voucher needs at least two lines:
- **Payment of ₹5,000 to supplier:**
  - Dr: Purchase Expenses ₹5,000 (DEBIT)
  - Cr: Cash in Hand ₹5,000 (CREDIT)
- **Receipt of ₹10,000 from customer:**
  - Dr: Bank Account ₹10,000 (DEBIT)
  - Cr: Sales Revenue ₹10,000 (CREDIT)

If debits ≠ credits, the system rejects the entry with a "422 Unbalanced" error.

### 3.4 Voucher Types Summary
| Type | Use Case | Shortcut |
|------|---------|---------|
| Payment (PMT) | Cash/bank payments going out | F5 |
| Receipt (RCP) | Cash/bank receipts coming in | F6 |
| Journal (JNL) | Adjusting entries, provisions | F7 |
| Contra (CTR) | Cash ↔ Bank transfers | F4 |
| Sales (SLS) | Revenue from customers | F8 |
| Purchase (PUR) | Goods/services purchased | F9 |
| Debit Note (DN) | Purchase return | — |
| Credit Note (CN) | Sales return | — |

### 3.5 Viewing Posted Vouchers
Navigate to **Accounting → Transactions** to see a paginated list. Use filters:
- Date range
- Voucher type
- Account involved
- Status (POSTED, DRAFT, PENDING_CHECK)

Click on any voucher to view its full details including all journal entry lines and audit history.

### 3.6 Reversing a Voucher
To correct a posted voucher:
1. Open the voucher
2. Click **Reverse Entry** button
3. Select reversal date and provide narration
4. The system creates a mirror-image voucher with all debits/credits swapped
5. Both the original and reversal remain in the audit trail

> **Important:** You cannot delete or modify a posted voucher. All corrections must be made through reversal entries.

---

## 4. Trial Balance and Reports

### 4.1 Trial Balance
Navigate to **Reports → Trial Balance** or press `Ctrl+K → "Trial Balance"`.

1. Set **From Date** and **To Date**
2. Optionally filter by **Cost Center** or **Branch**
3. Click **Generate** or press `Enter`

The report shows:
- Account Code | Name | Opening Dr | Opening Cr | Period Dr | Period Cr | Closing Dr | Closing Cr
- Footer totals (Total Debits = Total Credits — if not, data integrity issue)

**Export:** Click **Export PDF** or **Export Excel** buttons.  
**Performance:** First generation may take 2–5 seconds. Repeat requests are served from cache in < 100ms.

### 4.2 Profit & Loss Statement
Navigate to **Reports → Profit & Loss**.

1. Set date range (typically a financial year or quarter)
2. Toggle **Comparative Period** to see year-over-year comparison
3. Select **Cost Center** for departmental P&L

The report shows:
- Revenue (Gross) → COGS → Gross Profit → Operating Expenses → EBIT → Tax → Net Profit

### 4.3 Balance Sheet
Navigate to **Reports → Balance Sheet**.

1. Set **As Of Date** (point-in-time snapshot)
2. Toggle **Consolidated** to include all branches

The report shows:
- Assets: Current (Cash, AR, Inventory) + Non-Current (Fixed Assets, Investments)
- Liabilities: Current (AP, TDS Payable) + Non-Current (Term Loans)
- Equity: Share Capital + Retained Earnings
- Check: Total Assets = Total Liabilities + Equity

### 4.4 Cash Flow Statement
Navigate to **Reports → Cash Flow**.

Shows:
- **Operating Activities:** Net profit adjusted for non-cash items
- **Investing Activities:** Fixed asset purchases/sales
- **Financing Activities:** Loan repayments, dividends
- **Net Change in Cash** for the period

### 4.5 Exporting Reports
All reports support:
- **PDF** — Formatted for printing and sharing
- **Excel (XLSX)** — For further analysis in spreadsheets
- **CSV** — For data import into other systems

---

## 5. Bank Reconciliation

### 5.1 Overview
Bank Reconciliation matches your book entries with the bank statement to find discrepancies.

Navigate to **Banking → Reconciliation**.

### 5.2 Step-by-Step Reconciliation
**Step 1: Import Bank Statement**
1. Click **Import Statement**
2. Upload CSV/OFX/MT940 file from your bank
3. Select the **Bank Account** ledger account
4. Set the statement period

**Step 2: Auto-Matching**
The system automatically matches bank transactions with book entries using:
- Exact amount match
- Date within ±3 days
- Reference number (partial match)

Matched items show in green. Confidence < 85% items are flagged for manual review.

**Step 3: Manual Matching**
For unmatched items:
- Select an unmatched bank transaction
- Find the corresponding book entry and click **Match**
- Or if no book entry exists (bank charge not recorded), click **Create Entry** to post a new journal entry

**Step 4: Finalize**
Once all items are matched and the difference is ₹0:
1. Click **Finalize Reconciliation**
2. The period is locked — no further modifications allowed
3. Download the Reconciliation Statement for records

### 5.3 Reconciliation Report
The report shows:
```
Bank Statement Closing Balance:    ₹5,00,000
Add: Deposits in Transit:         +₹20,000
Less: Uncleared Cheques:          -₹15,000
Adjusted Bank Balance:            ₹5,05,000

Book Balance:                     ₹5,05,000
Difference:                       ₹0
```

---

## 6. Fixed Assets

### 6.1 Registering a New Asset
Navigate to **Accounting → Fixed Assets → Register New Asset**.

Fill in:
- **Asset Name:** e.g., "Dell Laptop Model XPS-15"
- **Asset Code:** Unique identifier (e.g., COMP-2026-001)
- **Category:** Computers & IT Equipment
- **Purchase Date:** Date of acquisition
- **Cost:** Purchase price (BigDecimal)
- **Residual Value:** Scrap value at end of life
- **Useful Life:** Years (for SLM)
- **Depreciation Method:** SLM (Straight-Line) or WDV (Written Down Value)

### 6.2 Computing Depreciation
1. Navigate to **Accounting → Fixed Assets**
2. Select the asset
3. Click **Run Depreciation** for the period
4. Review the computed amount
5. Click **Post** to auto-generate the depreciation journal entry

**SLM Formula:** `(Cost − Residual Value) / Useful Life`  
**WDV Formula:** `Opening Book Value × Depreciation Rate`

### 6.3 Asset Disposal
1. Navigate to the asset
2. Click **Dispose Asset**
3. Enter **Disposal Date** and **Sale Price**
4. System generates:
   - Dr Cash/Bank (sale proceeds)
   - Dr Accumulated Depreciation
   - Cr Fixed Asset (cost)
   - Cr/Dr Profit/Loss on Disposal

### 6.4 Fixed Asset Register Report
Navigate to **Reports → Fixed Asset Register** for a complete schedule with all assets, depreciation, and book values.

---

## 7. GST Compliance (TDS, TCS, e-Invoice)

### 7.1 TDS Automatic Deduction
When posting a **Payment Voucher**, add the payee's PAN and TDS section code:
1. In voucher entry, click **Tax Details**
2. Enter **Payee PAN** and **TDS Section** (e.g., 194J for professional services)
3. The system auto-computes TDS and adds the journal entries

**TDS Sections Supported:**
- 194A — Interest
- 194C — Contractors (1%/2%)
- 194H — Commission (5%)
- 194I — Rent (10%/2%)
- 194J — Professional/Technical (10%)
- 194Q — Purchase of goods (0.1%)

### 7.2 TDS Register and Returns
Navigate to **GST → TDS Register** to view all TDS deductions.

Export **Form 26Q data** for quarterly TDS returns filing:
1. Navigate to **GST → TDS Returns**
2. Select quarter (Q1/Q2/Q3/Q4) and year
3. Click **Export 26Q**

### 7.3 e-Invoice Generation
For B2B invoices ≥ ₹5 lakh:
1. Post a Sales Voucher with buyer's GSTIN
2. Navigate to the posted voucher
3. Click **Generate e-Invoice**
4. The system calls the GSTN API and retrieves the IRN
5. QR code is stored with the voucher and can be printed

### 7.4 e-Way Bill
For goods movement ≥ ₹50,000:
1. Navigate to **GST → e-Way Bill**
2. Fill transporter details (vehicle number, transporter GSTIN)
3. Click **Generate e-Way Bill**

### 7.5 GSTR-1 Export
Navigate to **GST → GSTR-1** to export B2B invoice data for the monthly GST return.

---

## 8. AI Dashboard

### 8.1 Cash Flow Forecasting
Navigate to **AI → Forecasting**.

1. Select **Forecast Horizon** (30, 60, 90, or 180 days)
2. Click **Generate Forecast**
3. View projected cash inflows and outflows with confidence bands:
   - **Base case** (solid line)
   - **Optimistic** (+20% scenario)
   - **Pessimistic** (−20% scenario)
4. Use **Scenario Modeling** to test custom assumptions

### 8.2 Anomaly Detection
Navigate to **AI → Anomaly Alerts**.

The system continuously monitors posted transactions. Alerts are generated when:
- Transaction amount > 3× historical average for the account
- Unusual counterparty (first-time payee with large amount)
- Transactions at unusual times (outside business hours)
- Frequency spikes (20+ transactions in 1 hour for normally low-volume account)

**Handling Alerts:**
- Click on an alert to see the full explanation
- Click **Dismiss** if it's a legitimate transaction (false positive)
- Click **Investigate** to open the flagged voucher

### 8.3 Mark-to-Market Valuation
Navigate to **Market → MTM** to see current market valuations for investment portfolios.

### 8.4 Accounts Receivable Dashboard
Navigate to **Receivable** to see outstanding receivables with aging analysis:
- Current (0–30 days)
- Overdue 31–60 days
- Overdue 61–90 days
- Overdue > 90 days

---

## 9. Command Palette

The **Command Palette** is the fastest way to navigate OneBook.

### Opening the Palette
- **Windows/Linux:** `Ctrl+K`
- **Mac:** `Cmd+K`

### Using the Palette
1. Start typing any keyword (screen name, voucher type, report name)
2. Use ↑↓ arrows to select from results
3. Press `Enter` to navigate/execute
4. Press `Escape` to close

### Example Commands
| Type | Result |
|------|--------|
| "payment" | → Open Payment Voucher (F5) |
| "trial" | → Open Trial Balance report |
| "reconcile" | → Open Bank Reconciliation |
| "chart" | → Open Chart of Accounts |
| "forecast" | → Open AI Forecasting |
| "anomaly" | → Open Anomaly Alerts |
| "fixed asset" | → Open Fixed Asset Register |
| "tds" | → Open TDS Register |

---

## 10. Keyboard Shortcuts Reference

### Voucher Entry Shortcuts (Tally-Compatible)
| Shortcut | Action |
|----------|--------|
| `F4` | Open Contra Voucher |
| `F5` | Open Payment Voucher |
| `F6` | Open Receipt Voucher |
| `F7` | Open Journal Voucher |
| `F8` | Open Sales Voucher |
| `F9` | Open Purchase Voucher |
| `Ctrl+A` | Accept / Save entry |
| `Escape` | Cancel / Close |
| `Alt+C` | Create new account (in account picker) |
| `Tab` | Move to next field |
| `Shift+Tab` | Move to previous field |

### Navigation Shortcuts
| Shortcut | Action |
|----------|--------|
| `Ctrl+K` / `Cmd+K` | Open Command Palette |
| `Alt+F4` | Close current screen |
| `Ctrl+Home` | Go to Dashboard |
| `Ctrl+R` | Refresh current report |
| `Ctrl+P` | Print current view |

### Report Shortcuts
| Shortcut | Action |
|----------|--------|
| `Ctrl+E` | Export current report |
| `Ctrl+Shift+P` | Export as PDF |
| `Ctrl+Shift+X` | Export as Excel |
| `←` `→` | Navigate date periods |

### Voucher List Shortcuts
| Shortcut | Action |
|----------|--------|
| `↑` `↓` | Navigate list items |
| `Enter` | Open selected item |
| `Ctrl+F` | Search/filter list |
| `Delete` | Request reversal (opens reversal dialog) |

---

## 11. Troubleshooting

### Application Won't Load
- Clear browser cache and hard-reload (`Ctrl+Shift+R`)
- Check browser console for errors (`F12`)
- Verify you are using a supported browser (Chrome 120+, Firefox 120+, Edge 120+)
- Check network connectivity to the API server

### "Session Expired" Error
Your JWT has expired (8-hour default). Click **Login Again** or navigate to the login page. Your unsaved work may be lost — save frequently with `Ctrl+A`.

### "422 Unbalanced Transaction" Error
Your journal entry has different debit and credit totals. Check:
- All entry line amounts are correct
- All DEBIT amounts sum to the same total as CREDIT amounts
- No accidental duplicate lines

### Reports Take Too Long
- First generation for large datasets (>1M entries) may take up to 5 seconds
- Repeat requests are cached and return in < 100ms
- If consistently slow, contact IT Admin to check Redis cache health

### "403 Forbidden" on Approval
You are attempting to approve a voucher that you created. OneBook enforces Maker ≠ Checker/Approver. Ask another user with Checker/Approver role to approve.

### Encrypted Data Not Displaying
If account names or narrations show garbled text, there may be an encryption key mismatch. Contact IT Admin to verify the `ONEBOOK_ENCRYPTION_KEY` environment variable is correctly configured.

### Bank Statement Import Fails
Ensure your file format is one of:
- CSV (comma-separated with headers: Date, Description, Reference, Amount, Type)
- OFX (Open Financial Exchange format)
- MT940 (SWIFT bank statement format)

Check that amounts use period (.) as decimal separator, not comma.

### TDS Not Calculating
Verify:
- Payee PAN is entered correctly (10-character format: ABCDE1234F)
- TDS section code is selected
- Payment amount exceeds the section threshold
- Lower deduction certificate (Form 13) has not overridden the rate

---

*For technical support, contact your IT Administrator or refer to `docs/developer-guide.md`.*

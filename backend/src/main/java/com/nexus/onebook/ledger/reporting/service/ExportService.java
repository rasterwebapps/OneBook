package com.nexus.onebook.ledger.reporting.service;

import com.nexus.onebook.ledger.accounts.dto.*;
import com.nexus.onebook.ledger.auditor.dto.*;
import com.nexus.onebook.ledger.banking.dto.*;
import com.nexus.onebook.ledger.clientaccount.dto.*;
import com.nexus.onebook.ledger.compliance.dto.*;
import com.nexus.onebook.ledger.credit.dto.*;
import com.nexus.onebook.ledger.currency.dto.*;
import com.nexus.onebook.ledger.entitlement.dto.*;
import com.nexus.onebook.ledger.fixedasset.dto.*;
import com.nexus.onebook.ledger.intelligence.dto.*;
import com.nexus.onebook.ledger.inventory.dto.*;
import com.nexus.onebook.ledger.operations.dto.*;
import com.nexus.onebook.ledger.payroll.dto.*;
import com.nexus.onebook.ledger.reporting.dto.*;
import com.nexus.onebook.ledger.tenant.dto.*;
import com.nexus.onebook.ledger.accounts.model.*;
import com.nexus.onebook.ledger.auditor.model.*;
import com.nexus.onebook.ledger.banking.model.*;
import com.nexus.onebook.ledger.clientaccount.model.*;
import com.nexus.onebook.ledger.compliance.model.*;
import com.nexus.onebook.ledger.credit.model.*;
import com.nexus.onebook.ledger.currency.model.*;
import com.nexus.onebook.ledger.entitlement.model.*;
import com.nexus.onebook.ledger.fixedasset.model.*;
import com.nexus.onebook.ledger.foundation.model.*;
import com.nexus.onebook.ledger.intelligence.model.*;
import com.nexus.onebook.ledger.inventory.model.*;
import com.nexus.onebook.ledger.operations.model.*;
import com.nexus.onebook.ledger.payroll.model.*;
import com.nexus.onebook.ledger.reporting.model.*;
import com.nexus.onebook.ledger.tenant.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.nexus.onebook.ledger.accounts.service.LedgerAccountService;
import com.nexus.onebook.ledger.accounts.service.TrialBalanceService;

/**
 * Export service — supports JSON and Excel export of financial data.
 * Provides structured export of ledger data, trial balances, and reports.
 */
@Service
public class ExportService {

    private final TrialBalanceService trialBalanceService;
    private final LedgerAccountService ledgerAccountService;

    public ExportService(TrialBalanceService trialBalanceService,
                         LedgerAccountService ledgerAccountService) {
        this.trialBalanceService = trialBalanceService;
        this.ledgerAccountService = ledgerAccountService;
    }

    /**
     * Exports trial balance as a structured map for JSON serialization.
     */
    public Map<String, Object> exportTrialBalanceAsJson(String tenantId) {
        TrialBalanceReport report = trialBalanceService.generateTrialBalance(tenantId);
        return Map.of(
                "reportType", "TRIAL_BALANCE",
                "tenantId", tenantId,
                "totalDebits", report.totalDebits(),
                "totalCredits", report.totalCredits(),
                "isBalanced", report.balanced(),
                "lines", report.lines()
        );
    }

    /**
     * Exports ledger accounts as a list of maps for JSON/CSV serialization.
     */
    public List<Map<String, Object>> exportLedgerAccountsAsJson(String tenantId) {
        List<LedgerAccount> accounts = ledgerAccountService.getAccountsByTenant(tenantId);
        return accounts.stream()
                .map(a -> Map.<String, Object>of(
                        "accountCode", a.getAccountCode(),
                        "accountName", a.getAccountName(),
                        "accountType", a.getAccountType().name(),
                        "isActive", a.isActive()
                ))
                .toList();
    }

    /**
     * Generates CSV-formatted string for ledger accounts export.
     */
    public String exportLedgerAccountsAsCsv(String tenantId) {
        List<LedgerAccount> accounts = ledgerAccountService.getAccountsByTenant(tenantId);
        StringBuilder csv = new StringBuilder("Account Code,Account Name,Account Type,Active\n");
        for (LedgerAccount account : accounts) {
            csv.append(account.getAccountCode()).append(",")
               .append(account.getAccountName()).append(",")
               .append(account.getAccountType().name()).append(",")
               .append(account.isActive()).append("\n");
        }
        return csv.toString();
    }
}

package com.nexus.onebook.reporting.service;

import com.nexus.onebook.accounts.dto.*;
import com.nexus.onebook.auditor.dto.*;
import com.nexus.onebook.banking.dto.*;
import com.nexus.onebook.clientaccount.dto.*;
import com.nexus.onebook.compliance.dto.*;
import com.nexus.onebook.credit.dto.*;
import com.nexus.onebook.currency.dto.*;
import com.nexus.onebook.entitlement.dto.*;
import com.nexus.onebook.fixedasset.dto.*;
import com.nexus.onebook.intelligence.dto.*;
import com.nexus.onebook.inventory.dto.*;
import com.nexus.onebook.operations.dto.*;
import com.nexus.onebook.payroll.dto.*;
import com.nexus.onebook.reporting.dto.*;
import com.nexus.onebook.tenant.dto.*;
import com.nexus.onebook.accounts.model.*;
import com.nexus.onebook.auditor.model.*;
import com.nexus.onebook.banking.model.*;
import com.nexus.onebook.clientaccount.model.*;
import com.nexus.onebook.compliance.model.*;
import com.nexus.onebook.credit.model.*;
import com.nexus.onebook.currency.model.*;
import com.nexus.onebook.entitlement.model.*;
import com.nexus.onebook.fixedasset.model.*;
import com.nexus.onebook.foundation.model.*;
import com.nexus.onebook.intelligence.model.*;
import com.nexus.onebook.inventory.model.*;
import com.nexus.onebook.operations.model.*;
import com.nexus.onebook.payroll.model.*;
import com.nexus.onebook.reporting.model.*;
import com.nexus.onebook.tenant.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.nexus.onebook.accounts.service.LedgerAccountService;
import com.nexus.onebook.accounts.service.TrialBalanceService;

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

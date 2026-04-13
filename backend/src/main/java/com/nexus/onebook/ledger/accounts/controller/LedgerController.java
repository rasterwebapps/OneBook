package com.nexus.onebook.ledger.accounts.controller;

import com.nexus.onebook.ledger.accounts.dto.LedgerAccountRequest;
import com.nexus.onebook.ledger.accounts.dto.TrialBalanceReport;
import com.nexus.onebook.ledger.accounts.model.CostCenter;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.accounts.repository.CostCenterRepository;
import com.nexus.onebook.ledger.accounts.service.LedgerAccountService;
import com.nexus.onebook.ledger.accounts.service.TrialBalanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Chart of Accounts and Trial Balance operations.
 */
@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerAccountService accountService;
    private final TrialBalanceService trialBalanceService;
    private final CostCenterRepository costCenterRepository;

    public LedgerController(LedgerAccountService accountService,
                            TrialBalanceService trialBalanceService,
                            CostCenterRepository costCenterRepository) {
        this.accountService = accountService;
        this.trialBalanceService = trialBalanceService;
        this.costCenterRepository = costCenterRepository;
    }

    @PostMapping("/accounts")
    public ResponseEntity<LedgerAccount> createAccount(@Valid @RequestBody LedgerAccountRequest request) {
        LedgerAccount account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<LedgerAccount>> getAccounts(@RequestParam String tenantId) {
        return ResponseEntity.ok(accountService.getAccountsByTenant(tenantId));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<LedgerAccount> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @GetMapping("/trial-balance")
    public ResponseEntity<TrialBalanceReport> getTrialBalance(
            @RequestParam String tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(trialBalanceService.generateTrialBalance(tenantId, fromDate, toDate));
    }

    @GetMapping("/cost-centers")
    public ResponseEntity<List<CostCenter>> getCostCenters(@RequestParam String tenantId) {
        return ResponseEntity.ok(costCenterRepository.findByTenantId(tenantId));
    }
}

package com.nexus.onebook.ledger.credit.controller;

import com.nexus.onebook.ledger.credit.dto.CreditLimitRequest;
import com.nexus.onebook.ledger.credit.model.CreditLimit;
import com.nexus.onebook.ledger.credit.service.CreditManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for Credit Management — set credit limits and monitor AR.
 */
@RestController
@RequestMapping("/api/credit-management")
public class CreditManagementController {

    private final CreditManagementService creditManagementService;

    public CreditManagementController(CreditManagementService creditManagementService) {
        this.creditManagementService = creditManagementService;
    }

    @PostMapping
    public ResponseEntity<CreditLimit> setCreditLimit(@Valid @RequestBody CreditLimitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creditManagementService.setCreditLimit(request));
    }

    @GetMapping
    public ResponseEntity<List<CreditLimit>> getCreditLimits(@RequestParam String tenantId) {
        return ResponseEntity.ok(creditManagementService.getCreditLimits(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditLimit> getCreditLimit(@PathVariable Long id) {
        return ResponseEntity.ok(creditManagementService.getCreditLimit(id));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkCredit(@RequestParam String tenantId,
                                                @RequestParam Long accountId,
                                                @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(creditManagementService.checkCreditAvailability(tenantId, accountId, amount));
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<CreditLimit>> getBlockedAccounts(@RequestParam String tenantId) {
        return ResponseEntity.ok(creditManagementService.getBlockedAccounts(tenantId));
    }
}

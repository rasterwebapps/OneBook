package com.nexus.onebook.credit.service;

import com.nexus.onebook.credit.dto.CreditLimitRequest;
import com.nexus.onebook.credit.model.CreditLimit;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.credit.repository.CreditLimitRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Credit Management service — set credit limits for customers
 * and monitor Accounts Receivable to prevent bad debts.
 */
@Service
public class CreditManagementService {

    private final CreditLimitRepository creditLimitRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    public CreditManagementService(CreditLimitRepository creditLimitRepository,
                                    LedgerAccountRepository ledgerAccountRepository) {
        this.creditLimitRepository = creditLimitRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Transactional
    public CreditLimit setCreditLimit(CreditLimitRequest request) {
        LedgerAccount account = ledgerAccountRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found: " + request.accountId()));

        CreditLimit creditLimit = creditLimitRepository
                .findByTenantIdAndAccountId(request.tenantId(), request.accountId())
                .orElse(new CreditLimit(request.tenantId(), account,
                        request.creditLimit(), request.creditPeriodDays()));

        creditLimit.setCreditLimit(request.creditLimit());
        creditLimit.setCreditPeriodDays(request.creditPeriodDays());
        creditLimit.setLastReviewedAt(Instant.now());
        return creditLimitRepository.save(creditLimit);
    }

    @Transactional(readOnly = true)
    public List<CreditLimit> getCreditLimits(String tenantId) {
        return creditLimitRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public CreditLimit getCreditLimit(Long id) {
        return creditLimitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit limit not found: " + id));
    }

    @Transactional(readOnly = true)
    public boolean checkCreditAvailability(String tenantId, Long accountId, BigDecimal transactionAmount) {
        return creditLimitRepository.findByTenantIdAndAccountId(tenantId, accountId)
                .map(cl -> {
                    if (cl.isBlocked()) return false;
                    BigDecimal availableCredit = cl.getCreditLimit().subtract(cl.getCurrentOutstanding());
                    return availableCredit.compareTo(transactionAmount) >= 0;
                })
                .orElse(true); // No limit set means unlimited
    }

    @Transactional
    public CreditLimit updateOutstanding(String tenantId, Long accountId, BigDecimal amount) {
        CreditLimit creditLimit = creditLimitRepository
                .findByTenantIdAndAccountId(tenantId, accountId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No credit limit configured for account: " + accountId));

        creditLimit.setCurrentOutstanding(creditLimit.getCurrentOutstanding().add(amount));

        // Auto-block if over limit
        if (creditLimit.getCurrentOutstanding().compareTo(creditLimit.getCreditLimit()) > 0) {
            creditLimit.setBlocked(true);
        }

        return creditLimitRepository.save(creditLimit);
    }

    @Transactional(readOnly = true)
    public List<CreditLimit> getBlockedAccounts(String tenantId) {
        return creditLimitRepository.findByTenantIdAndBlockedTrue(tenantId);
    }
}

package com.nexus.onebook.credit.service;

import com.nexus.onebook.credit.dto.CreditLimitRequest;
import com.nexus.onebook.credit.model.CreditLimit;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.credit.repository.CreditLimitRepository;
import com.nexus.onebook.accounts.repository.LedgerAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditManagementServiceTest {

    @Mock
    private CreditLimitRepository creditLimitRepository;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @InjectMocks
    private CreditManagementService creditManagementService;

    @Test
    void setCreditLimit_validAccount_createsLimit() {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        CreditLimitRequest request = new CreditLimitRequest(
                "tenant-1", 1L, new BigDecimal("50000"), 30);

        when(ledgerAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(creditLimitRepository.findByTenantIdAndAccountId("tenant-1", 1L))
                .thenReturn(Optional.empty());
        when(creditLimitRepository.save(any(CreditLimit.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreditLimit result = creditManagementService.setCreditLimit(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("50000"), result.getCreditLimit());
        assertEquals(30, result.getCreditPeriodDays());
    }

    @Test
    void setCreditLimit_accountNotFound_throwsException() {
        CreditLimitRequest request = new CreditLimitRequest(
                "tenant-1", 99L, new BigDecimal("50000"), 30);

        when(ledgerAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                creditManagementService.setCreditLimit(request));
    }

    @Test
    void getCreditLimit_exists_returnsLimit() {
        CreditLimit cl = new CreditLimit();
        cl.setId(1L);
        when(creditLimitRepository.findById(1L)).thenReturn(Optional.of(cl));

        CreditLimit result = creditManagementService.getCreditLimit(1L);

        assertNotNull(result);
    }

    @Test
    void getCreditLimit_notFound_throwsException() {
        when(creditLimitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                creditManagementService.getCreditLimit(99L));
    }

    @Test
    void checkCreditAvailability_noLimitSet_returnsTrue() {
        when(creditLimitRepository.findByTenantIdAndAccountId("tenant-1", 1L))
                .thenReturn(Optional.empty());

        boolean result = creditManagementService.checkCreditAvailability(
                "tenant-1", 1L, new BigDecimal("10000"));

        assertTrue(result);
    }

    @Test
    void checkCreditAvailability_blocked_returnsFalse() {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        CreditLimit cl = new CreditLimit("tenant-1", account, new BigDecimal("50000"), 30);
        cl.setBlocked(true);

        when(creditLimitRepository.findByTenantIdAndAccountId("tenant-1", 1L))
                .thenReturn(Optional.of(cl));

        boolean result = creditManagementService.checkCreditAvailability(
                "tenant-1", 1L, new BigDecimal("1000"));

        assertFalse(result);
    }

    @Test
    void updateOutstanding_overLimit_autoBlocks() {
        LedgerAccount account = new LedgerAccount();
        account.setId(1L);
        CreditLimit cl = new CreditLimit("tenant-1", account, new BigDecimal("10000"), 30);
        cl.setCurrentOutstanding(new BigDecimal("9000"));

        when(creditLimitRepository.findByTenantIdAndAccountId("tenant-1", 1L))
                .thenReturn(Optional.of(cl));
        when(creditLimitRepository.save(any(CreditLimit.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreditLimit result = creditManagementService.updateOutstanding(
                "tenant-1", 1L, new BigDecimal("2000"));

        assertTrue(result.isBlocked());
        assertEquals(new BigDecimal("11000"), result.getCurrentOutstanding());
    }
}

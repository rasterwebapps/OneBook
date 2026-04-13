package com.nexus.onebook.voucher.service;

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
import com.nexus.onebook.accounts.repository.*;
import com.nexus.onebook.auditor.repository.*;
import com.nexus.onebook.banking.repository.*;
import com.nexus.onebook.clientaccount.repository.*;
import com.nexus.onebook.compliance.repository.*;
import com.nexus.onebook.credit.repository.*;
import com.nexus.onebook.currency.repository.*;
import com.nexus.onebook.entitlement.repository.*;
import com.nexus.onebook.fixedasset.repository.*;
import com.nexus.onebook.foundation.repository.*;
import com.nexus.onebook.intelligence.repository.*;
import com.nexus.onebook.inventory.repository.*;
import com.nexus.onebook.operations.repository.*;
import com.nexus.onebook.payroll.repository.*;
import com.nexus.onebook.reporting.repository.*;
import com.nexus.onebook.tenant.repository.*;
import com.nexus.onebook.voucher.dto.*;
import com.nexus.onebook.voucher.model.*;
import com.nexus.onebook.voucher.repository.ReceiptRepository;
import com.nexus.onebook.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock private ReceiptRepository receiptRepository;
    @Mock private VoucherRepository voucherRepository;
    @Mock private PayerRepository payerRepository;
    @Mock private PayerBankAccountRepository payerBankAccountRepository;
    @Mock private PayeeRepository payeeRepository;
    @Mock private PayeeBankAccountRepository payeeBankAccountRepository;
    @Mock private LedgerAccountRepository ledgerAccountRepository;

    @InjectMocks
    private ReceiptService receiptService;

    private Receipt sampleReceipt;

    @BeforeEach
    void setUp() {
        sampleReceipt = new Receipt("tenant1", "RCT-001", new BigDecimal("5000.0000"), "admin");
        sampleReceipt.setId(1L);
    }

    @Test
    void createReceipt_shouldPersistAndReturn() {
        CreateReceiptRequest request = new CreateReceiptRequest(
                "tenant1", "RCT-001", null, null, null, null, null,
                null, null, new BigDecimal("5000.0000"), null, null,
                Instant.now(), "Payment received", "admin");

        when(receiptRepository.save(any(Receipt.class))).thenAnswer(i -> {
            Receipt r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReceiptResponse response = receiptService.createReceipt(request);

        assertNotNull(response);
        assertEquals("RCT-001", response.receiptNumber());
        assertEquals("CREATED", response.status());
        assertEquals(new BigDecimal("5000.0000"), response.amount());
    }

    @Test
    void getReceiptsByTenant_shouldReturnList() {
        when(receiptRepository.findByTenantId("tenant1")).thenReturn(List.of(sampleReceipt));

        List<ReceiptResponse> results = receiptService.getReceiptsByTenant("tenant1");

        assertEquals(1, results.size());
        assertEquals("RCT-001", results.get(0).receiptNumber());
    }

    @Test
    void getReceiptById_shouldReturnReceipt() {
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(sampleReceipt));

        ReceiptResponse response = receiptService.getReceiptById(1L);

        assertNotNull(response);
        assertEquals("RCT-001", response.receiptNumber());
    }

    @Test
    void getReceiptById_shouldThrowWhenNotFound() {
        when(receiptRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> receiptService.getReceiptById(999L));
    }
}

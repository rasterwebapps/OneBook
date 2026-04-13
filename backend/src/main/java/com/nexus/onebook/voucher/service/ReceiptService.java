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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final VoucherRepository voucherRepository;
    private final PayerRepository payerRepository;
    private final PayerBankAccountRepository payerBankAccountRepository;
    private final PayeeRepository payeeRepository;
    private final PayeeBankAccountRepository payeeBankAccountRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    public ReceiptService(ReceiptRepository receiptRepository,
                          VoucherRepository voucherRepository,
                          PayerRepository payerRepository,
                          PayerBankAccountRepository payerBankAccountRepository,
                          PayeeRepository payeeRepository,
                          PayeeBankAccountRepository payeeBankAccountRepository,
                          LedgerAccountRepository ledgerAccountRepository) {
        this.receiptRepository = receiptRepository;
        this.voucherRepository = voucherRepository;
        this.payerRepository = payerRepository;
        this.payerBankAccountRepository = payerBankAccountRepository;
        this.payeeRepository = payeeRepository;
        this.payeeBankAccountRepository = payeeBankAccountRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Transactional
    public ReceiptResponse createReceipt(CreateReceiptRequest request) {
        Receipt receipt = new Receipt(request.tenantId(), request.receiptNumber(),
                request.amount(), request.createdBy());

        if (request.voucherId() != null) {
            receipt.setVoucher(voucherRepository.findById(request.voucherId()).orElse(null));
        }
        if (request.payerId() != null) {
            receipt.setPayer(payerRepository.findById(request.payerId()).orElse(null));
        }
        if (request.payerBankAccountId() != null) {
            receipt.setPayerBankAccount(payerBankAccountRepository.findById(request.payerBankAccountId()).orElse(null));
        }
        if (request.payeeId() != null) {
            receipt.setPayee(payeeRepository.findById(request.payeeId()).orElse(null));
        }
        if (request.payeeBankAccountId() != null) {
            receipt.setPayeeBankAccount(payeeBankAccountRepository.findById(request.payeeBankAccountId()).orElse(null));
        }
        if (request.fromLedgerAccountId() != null) {
            receipt.setFromLedgerAccount(ledgerAccountRepository.findById(request.fromLedgerAccountId()).orElse(null));
        }
        if (request.toLedgerAccountId() != null) {
            receipt.setToLedgerAccount(ledgerAccountRepository.findById(request.toLedgerAccountId()).orElse(null));
        }
        if (request.paymentMode() != null) {
            receipt.setPaymentMode(PaymentMode.valueOf(request.paymentMode()));
        }
        receipt.setReferenceNumber(request.referenceNumber());
        receipt.setReceiptDate(request.receiptDate());
        receipt.setDescription(request.description());

        Receipt saved = receiptRepository.save(receipt);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByTenant(String tenantId) {
        return receiptRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptById(Long id) {
        Receipt r = receiptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found: " + id));
        return toResponse(r);
    }

    private ReceiptResponse toResponse(Receipt r) {
        return new ReceiptResponse(
                r.getId(), r.getTenantId(), r.getReceiptNumber(),
                r.getVoucher() != null ? r.getVoucher().getId() : null,
                r.getPayer() != null ? r.getPayer().getName() : null,
                r.getPayee() != null ? r.getPayee().getName() : null,
                r.getAmount(),
                r.getPaymentMode() != null ? r.getPaymentMode().name() : null,
                r.getReferenceNumber(), r.getStatus().name(),
                r.getReceiptDate(), r.getDescription(),
                r.getCreatedBy(), r.getCreatedAt()
        );
    }
}

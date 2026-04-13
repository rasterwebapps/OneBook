package com.nexus.onebook.ledger.voucher.service;

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
import com.nexus.onebook.ledger.accounts.repository.*;
import com.nexus.onebook.ledger.auditor.repository.*;
import com.nexus.onebook.ledger.banking.repository.*;
import com.nexus.onebook.ledger.clientaccount.repository.*;
import com.nexus.onebook.ledger.compliance.repository.*;
import com.nexus.onebook.ledger.credit.repository.*;
import com.nexus.onebook.ledger.currency.repository.*;
import com.nexus.onebook.ledger.entitlement.repository.*;
import com.nexus.onebook.ledger.fixedasset.repository.*;
import com.nexus.onebook.ledger.foundation.repository.*;
import com.nexus.onebook.ledger.intelligence.repository.*;
import com.nexus.onebook.ledger.inventory.repository.*;
import com.nexus.onebook.ledger.operations.repository.*;
import com.nexus.onebook.ledger.payroll.repository.*;
import com.nexus.onebook.ledger.reporting.repository.*;
import com.nexus.onebook.ledger.tenant.repository.*;
import com.nexus.onebook.ledger.voucher.dto.*;
import com.nexus.onebook.ledger.voucher.model.*;
import com.nexus.onebook.ledger.voucher.repository.PaymentAdviceRepository;
import com.nexus.onebook.ledger.voucher.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentAdviceService {

    private final PaymentAdviceRepository paymentAdviceRepository;
    private final VoucherRepository voucherRepository;
    private final ApplicationRepository applicationRepository;
    private final DepartmentRepository departmentRepository;
    private final PayerRepository payerRepository;
    private final PayerBankAccountRepository payerBankAccountRepository;
    private final PayeeRepository payeeRepository;
    private final PayeeBankAccountRepository payeeBankAccountRepository;

    public PaymentAdviceService(PaymentAdviceRepository paymentAdviceRepository,
                                VoucherRepository voucherRepository,
                                ApplicationRepository applicationRepository,
                                DepartmentRepository departmentRepository,
                                PayerRepository payerRepository,
                                PayerBankAccountRepository payerBankAccountRepository,
                                PayeeRepository payeeRepository,
                                PayeeBankAccountRepository payeeBankAccountRepository) {
        this.paymentAdviceRepository = paymentAdviceRepository;
        this.voucherRepository = voucherRepository;
        this.applicationRepository = applicationRepository;
        this.departmentRepository = departmentRepository;
        this.payerRepository = payerRepository;
        this.payerBankAccountRepository = payerBankAccountRepository;
        this.payeeRepository = payeeRepository;
        this.payeeBankAccountRepository = payeeBankAccountRepository;
    }

    @Transactional
    public PaymentAdviceResponse createPaymentAdvice(CreatePaymentAdviceRequest request) {
        PaymentAdvice pa = new PaymentAdvice(request.tenantId(), request.adviceNumber(),
                request.amount(), request.createdBy());

        if (request.voucherId() != null) {
            pa.setVoucher(voucherRepository.findById(request.voucherId()).orElse(null));
        }
        if (request.applicationId() != null) {
            pa.setApplication(applicationRepository.findById(request.applicationId()).orElse(null));
        }
        if (request.departmentId() != null) {
            pa.setDepartment(departmentRepository.findById(request.departmentId()).orElse(null));
        }
        if (request.payerId() != null) {
            pa.setPayer(payerRepository.findById(request.payerId()).orElse(null));
        }
        if (request.payerBankAccountId() != null) {
            pa.setPayerBankAccount(payerBankAccountRepository.findById(request.payerBankAccountId()).orElse(null));
        }
        if (request.payeeId() != null) {
            pa.setPayee(payeeRepository.findById(request.payeeId()).orElse(null));
        }
        if (request.payeeBankAccountId() != null) {
            pa.setPayeeBankAccount(payeeBankAccountRepository.findById(request.payeeBankAccountId()).orElse(null));
        }
        if (request.paymentMode() != null) {
            pa.setPaymentMode(PaymentMode.valueOf(request.paymentMode()));
        }
        if (request.twoStepVerification() != null) {
            pa.setTwoStepVerification(TwoStepVerificationType.valueOf(request.twoStepVerification()));
        }
        pa.setDescription(request.description());

        PaymentAdvice saved = paymentAdviceRepository.save(pa);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentAdviceResponse> getByTenant(String tenantId) {
        return paymentAdviceRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentAdviceResponse getById(Long id) {
        PaymentAdvice pa = paymentAdviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment advice not found: " + id));
        return toResponse(pa);
    }

    @Transactional
    public PaymentAdviceResponse approve(Long id, String approvedBy) {
        PaymentAdvice pa = paymentAdviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment advice not found: " + id));
        if (pa.getStatus() != PaymentAdviceStatus.PENDING) {
            throw new IllegalStateException("Payment advice must be in PENDING status to approve");
        }
        pa.setStatus(PaymentAdviceStatus.APPROVED);
        pa.setApprovedBy(approvedBy);
        pa.setApprovedAt(Instant.now());
        return toResponse(paymentAdviceRepository.save(pa));
    }

    @Transactional
    public PaymentAdviceResponse reject(Long id, String rejectedBy, String reason) {
        PaymentAdvice pa = paymentAdviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment advice not found: " + id));
        pa.setStatus(PaymentAdviceStatus.REJECTED);
        pa.setRejectedBy(rejectedBy);
        pa.setRejectedAt(Instant.now());
        pa.setRejectionReason(reason);
        return toResponse(paymentAdviceRepository.save(pa));
    }

    @Transactional
    public PaymentAdviceResponse markPaid(Long id, String transactionReference) {
        PaymentAdvice pa = paymentAdviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment advice not found: " + id));
        if (pa.getStatus() != PaymentAdviceStatus.APPROVED) {
            throw new IllegalStateException("Payment advice must be APPROVED to mark as paid");
        }
        pa.setStatus(PaymentAdviceStatus.PAID);
        pa.setPaidAt(Instant.now());
        pa.setTransactionReference(transactionReference);
        return toResponse(paymentAdviceRepository.save(pa));
    }

    private PaymentAdviceResponse toResponse(PaymentAdvice pa) {
        return new PaymentAdviceResponse(
                pa.getId(), pa.getTenantId(), pa.getAdviceNumber(),
                pa.getVoucher() != null ? pa.getVoucher().getId() : null,
                pa.getDepartment() != null ? pa.getDepartment().getName() : null,
                pa.getPayer() != null ? pa.getPayer().getName() : null,
                pa.getPayee() != null ? pa.getPayee().getName() : null,
                pa.getAmount(),
                pa.getPaymentMode() != null ? pa.getPaymentMode().name() : null,
                pa.getStatus().name(),
                pa.getTwoStepVerification() != null ? pa.getTwoStepVerification().name() : null,
                pa.getApprovedBy(), pa.getApprovedAt(),
                pa.getRejectedBy(), pa.getRejectionReason(),
                pa.getPaidAt(), pa.getTransactionReference(),
                pa.getDescription(), pa.getCreatedBy(), pa.getCreatedAt()
        );
    }
}

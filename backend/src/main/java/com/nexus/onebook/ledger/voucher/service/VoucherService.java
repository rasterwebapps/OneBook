package com.nexus.onebook.ledger.voucher.service;

import com.nexus.onebook.ledger.model.*;
import com.nexus.onebook.ledger.repository.*;
import com.nexus.onebook.ledger.voucher.dto.*;
import com.nexus.onebook.ledger.voucher.model.*;
import com.nexus.onebook.ledger.voucher.repository.VoucherItemRepository;
import com.nexus.onebook.ledger.voucher.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherItemRepository voucherItemRepository;
    private final VoucherTypeRepository voucherTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final SubDepartmentRepository subDepartmentRepository;
    private final PayerRepository payerRepository;
    private final PayerBankAccountRepository payerBankAccountRepository;
    private final PayeeRepository payeeRepository;
    private final PayeeBankAccountRepository payeeBankAccountRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final CostCenterRepository costCenterRepository;

    public VoucherService(VoucherRepository voucherRepository,
                          VoucherItemRepository voucherItemRepository,
                          VoucherTypeRepository voucherTypeRepository,
                          DepartmentRepository departmentRepository,
                          SubDepartmentRepository subDepartmentRepository,
                          PayerRepository payerRepository,
                          PayerBankAccountRepository payerBankAccountRepository,
                          PayeeRepository payeeRepository,
                          PayeeBankAccountRepository payeeBankAccountRepository,
                          LedgerAccountRepository ledgerAccountRepository,
                          CostCenterRepository costCenterRepository) {
        this.voucherRepository = voucherRepository;
        this.voucherItemRepository = voucherItemRepository;
        this.voucherTypeRepository = voucherTypeRepository;
        this.departmentRepository = departmentRepository;
        this.subDepartmentRepository = subDepartmentRepository;
        this.payerRepository = payerRepository;
        this.payerBankAccountRepository = payerBankAccountRepository;
        this.payeeRepository = payeeRepository;
        this.payeeBankAccountRepository = payeeBankAccountRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.costCenterRepository = costCenterRepository;
    }

    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        Voucher voucher = new Voucher(request.tenantId(), request.voucherNumber(),
                request.totalAmount(), request.netAmount(), request.createdBy());

        if (request.voucherTypeId() != null) {
            voucher.setVoucherType(voucherTypeRepository.findById(request.voucherTypeId()).orElse(null));
        }
        if (request.departmentId() != null) {
            voucher.setDepartment(departmentRepository.findById(request.departmentId()).orElse(null));
        }
        if (request.subDepartmentId() != null) {
            voucher.setSubDepartment(subDepartmentRepository.findById(request.subDepartmentId()).orElse(null));
        }
        if (request.payerId() != null) {
            voucher.setPayer(payerRepository.findById(request.payerId()).orElse(null));
        }
        if (request.payerBankAccountId() != null) {
            voucher.setPayerBankAccount(payerBankAccountRepository.findById(request.payerBankAccountId()).orElse(null));
        }
        if (request.paymentMode() != null) {
            voucher.setPaymentMode(PaymentMode.valueOf(request.paymentMode()));
        }
        voucher.setVoucherDate(request.voucherDate());
        voucher.setTdsAmount(request.tdsAmount() != null ? request.tdsAmount() : BigDecimal.ZERO);
        voucher.setDescription(request.description());
        voucher.setRemarks(request.remarks());

        Voucher saved = voucherRepository.save(voucher);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getVouchersByTenant(String tenantId) {
        return voucherRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found: " + id));
        return toResponse(v);
    }

    @Transactional
    public VoucherResponse approveVoucher(Long id, String approvedBy) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found: " + id));
        if (v.getStatus() != VoucherStatus.CREATED) {
            throw new IllegalStateException("Voucher must be in CREATED status to approve");
        }
        v.setStatus(VoucherStatus.APPROVED);
        v.setApprovedBy(approvedBy);
        v.setApprovedAt(Instant.now());
        v.setApprovedAmount(v.getTotalAmount());
        return toResponse(voucherRepository.save(v));
    }

    @Transactional
    public VoucherResponse cancelVoucher(Long id, String cancelledBy, String reason) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found: " + id));
        v.setCancelled(true);
        v.setCancelledBy(cancelledBy);
        v.setCancelledAt(Instant.now());
        v.setCancellationReason(reason);
        return toResponse(voucherRepository.save(v));
    }

    @Transactional
    public VoucherItemResponse createVoucherItem(CreateVoucherItemRequest request) {
        Voucher voucher = voucherRepository.findById(request.voucherId())
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found: " + request.voucherId()));

        VoucherItem item = new VoucherItem(request.tenantId(), voucher, request.amount(), request.netAmount());
        item.setItemNumber(request.itemNumber());
        item.setDescription(request.description());
        item.setTdsApplicable(request.tdsApplicable());
        item.setTdsPercentage(request.tdsPercentage());
        item.setTdsAmount(request.tdsAmount() != null ? request.tdsAmount() : BigDecimal.ZERO);
        item.setRemarks(request.remarks());

        if (request.payeeId() != null) {
            item.setPayee(payeeRepository.findById(request.payeeId()).orElse(null));
        }
        if (request.payeeBankAccountId() != null) {
            item.setPayeeBankAccount(payeeBankAccountRepository.findById(request.payeeBankAccountId()).orElse(null));
        }
        if (request.ledgerAccountId() != null) {
            item.setLedgerAccount(ledgerAccountRepository.findById(request.ledgerAccountId()).orElse(null));
        }
        if (request.costCenterId() != null) {
            item.setCostCenter(costCenterRepository.findById(request.costCenterId()).orElse(null));
        }

        VoucherItem saved = voucherItemRepository.save(item);
        return toItemResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VoucherItemResponse> getVoucherItems(String tenantId, Long voucherId) {
        return voucherItemRepository.findByTenantIdAndVoucherId(tenantId, voucherId).stream()
                .map(this::toItemResponse).toList();
    }

    private VoucherResponse toResponse(Voucher v) {
        return new VoucherResponse(
                v.getId(), v.getTenantId(), v.getVoucherNumber(),
                v.getVoucherType() != null ? v.getVoucherType().getVoucherName() : null,
                v.getDepartment() != null ? v.getDepartment().getName() : null,
                v.getPayer() != null ? v.getPayer().getName() : null,
                v.getVoucherDate(), v.getStatus().name(),
                v.getClosureType() != null ? v.getClosureType().name() : null,
                v.getTotalAmount(), v.getApprovedAmount(), v.getTdsAmount(), v.getNetAmount(),
                v.getPaymentMode() != null ? v.getPaymentMode().name() : null,
                v.getDescription(), v.getRemarks(), v.getApprovedBy(), v.getApprovedAt(),
                v.isCancelled(), v.getCreatedBy(), v.getCreatedAt()
        );
    }

    private VoucherItemResponse toItemResponse(VoucherItem i) {
        return new VoucherItemResponse(
                i.getId(), i.getTenantId(), i.getVoucher().getId(), i.getItemNumber(),
                i.getPayee() != null ? i.getPayee().getName() : null,
                i.getLedgerAccount() != null ? i.getLedgerAccount().getAccountName() : null,
                i.getCostCenter() != null ? i.getCostCenter().getName() : null,
                i.getDescription(), i.getAmount(), i.isTdsApplicable(),
                i.getTdsPercentage(), i.getTdsAmount(), i.getNetAmount(),
                i.getStatus().name(), i.getRemarks(), i.getCreatedAt()
        );
    }
}

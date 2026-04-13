package com.nexus.onebook.ledger.accounts.service;

import com.nexus.onebook.ledger.accounts.model.VoucherCategory;
import com.nexus.onebook.ledger.accounts.model.VoucherType;
import com.nexus.onebook.ledger.accounts.repository.VoucherTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Voucher Type service — manages configurable voucher types per tenant.
 * Supports Sales, Purchase, Payment, Receipt, Contra, Journal, Credit Note, Debit Note.
 */
@Service
public class VoucherTypeService {

    private final VoucherTypeRepository voucherTypeRepository;

    public VoucherTypeService(VoucherTypeRepository voucherTypeRepository) {
        this.voucherTypeRepository = voucherTypeRepository;
    }

    @Transactional
    public VoucherType createVoucherType(String tenantId, String voucherCode,
                                          String voucherName, String category) {
        voucherTypeRepository.findByTenantIdAndVoucherCode(tenantId, voucherCode)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Voucher type '" + voucherCode + "' already exists");
                });

        VoucherType vt = new VoucherType(tenantId, voucherCode, voucherName,
                VoucherCategory.valueOf(category));
        return voucherTypeRepository.save(vt);
    }

    @Transactional(readOnly = true)
    public List<VoucherType> getVoucherTypes(String tenantId) {
        return voucherTypeRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<VoucherType> getVoucherTypesByCategory(String tenantId, VoucherCategory category) {
        return voucherTypeRepository.findByTenantIdAndCategory(tenantId, category);
    }
}

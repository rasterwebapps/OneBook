package com.nexus.onebook.ledger.accounts.repository;

import com.nexus.onebook.ledger.accounts.model.VoucherCategory;
import com.nexus.onebook.ledger.accounts.model.VoucherType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherTypeRepository extends JpaRepository<VoucherType, Long> {
    List<VoucherType> findByTenantId(String tenantId);
    List<VoucherType> findByTenantIdAndCategory(String tenantId, VoucherCategory category);
    Optional<VoucherType> findByTenantIdAndVoucherCode(String tenantId, String voucherCode);
}

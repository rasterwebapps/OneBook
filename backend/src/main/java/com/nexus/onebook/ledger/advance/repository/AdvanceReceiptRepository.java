package com.nexus.onebook.ledger.advance.repository;

import com.nexus.onebook.ledger.advance.model.AdvanceReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvanceReceiptRepository extends JpaRepository<AdvanceReceipt, Long> {

    Optional<AdvanceReceipt> findByIdAndTenantId(Long id, String tenantId);

    List<AdvanceReceipt> findByTenantIdAndEmployeeId(String tenantId, Long employeeId);
}

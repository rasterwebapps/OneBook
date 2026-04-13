package com.nexus.onebook.compliance.repository;

import com.nexus.onebook.compliance.model.EInvoice;
import com.nexus.onebook.compliance.model.EInvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EInvoiceRepository extends JpaRepository<EInvoice, Long> {
    List<EInvoice> findByTenantId(String tenantId);
    List<EInvoice> findByTenantIdAndStatus(String tenantId, EInvoiceStatus status);
    Optional<EInvoice> findByTenantIdAndInvoiceNumber(String tenantId, String invoiceNumber);
}

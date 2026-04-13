package com.nexus.onebook.ingestion.repository;

import com.nexus.onebook.ingestion.model.VendorInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorInvoiceRepository extends JpaRepository<VendorInvoice, Long> {

    Optional<VendorInvoice> findByTenantIdAndInvoiceNumber(String tenantId, String invoiceNumber);

    Optional<VendorInvoice> findByTenantIdAndPoNumber(String tenantId, String poNumber);
}

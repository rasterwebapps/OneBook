package com.nexus.onebook.ledger.operations.repository;

import com.nexus.onebook.ledger.operations.model.VaultDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaultDocumentRepository extends JpaRepository<VaultDocument, Long> {

    List<VaultDocument> findByTenantId(String tenantId);

    List<VaultDocument> findByJournalTransactionId(Long journalTransactionId);
}

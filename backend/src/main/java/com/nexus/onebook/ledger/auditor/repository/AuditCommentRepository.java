package com.nexus.onebook.ledger.auditor.repository;

import com.nexus.onebook.ledger.auditor.model.AuditComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditCommentRepository extends JpaRepository<AuditComment, Long> {

    List<AuditComment> findByTenantId(String tenantId);

    List<AuditComment> findByTenantIdAndTableNameAndRecordId(
            String tenantId, String tableName, Long recordId);

    List<AuditComment> findByTenantIdAndResolved(String tenantId, boolean resolved);
}

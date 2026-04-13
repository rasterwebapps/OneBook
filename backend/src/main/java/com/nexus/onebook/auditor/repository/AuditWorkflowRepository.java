package com.nexus.onebook.auditor.repository;

import com.nexus.onebook.auditor.model.AuditWorkflow;
import com.nexus.onebook.auditor.model.AuditWorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditWorkflowRepository extends JpaRepository<AuditWorkflow, Long> {

    List<AuditWorkflow> findByTenantId(String tenantId);

    List<AuditWorkflow> findByTenantIdAndStatus(String tenantId, AuditWorkflowStatus status);
}

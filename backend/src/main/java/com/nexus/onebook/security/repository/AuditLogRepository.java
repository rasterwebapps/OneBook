package com.nexus.onebook.security.repository;

import com.nexus.onebook.security.model.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    /**
     * Returns the most recent audit entry for the given tenant,
     * ordered by ID descending so that the first result is the latest.
     */
    @Query("SELECT a FROM AuditLogEntry a WHERE a.tenantId = :tenantId ORDER BY a.id DESC LIMIT 1")
    Optional<AuditLogEntry> findLatestByTenantId(@Param("tenantId") String tenantId);

    /**
     * Returns all audit entries for a given tenant in insertion order.
     */
    List<AuditLogEntry> findByTenantIdOrderByIdAsc(String tenantId);

    /**
     * Returns audit entries for a specific record in insertion order.
     */
    List<AuditLogEntry> findByTenantIdAndTableNameAndRecordIdOrderByIdAsc(
            String tenantId, String tableName, Long recordId);
}

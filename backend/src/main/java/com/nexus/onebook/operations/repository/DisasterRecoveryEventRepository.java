package com.nexus.onebook.operations.repository;

import com.nexus.onebook.operations.model.DisasterRecoveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisasterRecoveryEventRepository extends JpaRepository<DisasterRecoveryEvent, Long> {

    List<DisasterRecoveryEvent> findByTenantId(String tenantId);

    List<DisasterRecoveryEvent> findByTenantIdAndEventType(String tenantId, String eventType);

    List<DisasterRecoveryEvent> findByTenantIdAndStatus(String tenantId, String status);
}

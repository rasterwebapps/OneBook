package com.nexus.onebook.ledger.operations.service;

import com.nexus.onebook.ledger.operations.dto.DisasterRecoveryRequest;
import com.nexus.onebook.ledger.operations.model.DisasterRecoveryEvent;
import com.nexus.onebook.ledger.operations.repository.DisasterRecoveryEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Disaster recovery service.
 * Manages automated backups, point-in-time recovery, and failover procedures.
 */
@Service
public class DisasterRecoveryService {

    private static final List<String> VALID_EVENT_TYPES = List.of(
            "FULL_BACKUP", "INCREMENTAL_BACKUP", "POINT_IN_TIME_RECOVERY", "FAILOVER", "FAILBACK");

    private final DisasterRecoveryEventRepository eventRepository;

    public DisasterRecoveryService(DisasterRecoveryEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Initiates a disaster recovery operation (backup, recovery, or failover).
     */
    @Transactional
    public DisasterRecoveryEvent initiateEvent(DisasterRecoveryRequest request) {
        if (!VALID_EVENT_TYPES.contains(request.eventType())) {
            throw new IllegalArgumentException("Invalid event type: " + request.eventType()
                    + ". Must be one of: " + VALID_EVENT_TYPES);
        }

        DisasterRecoveryEvent event = new DisasterRecoveryEvent(
                request.tenantId(), request.eventType());

        if (request.backupLocation() != null) {
            event.setBackupLocation(request.backupLocation());
        } else {
            // Generate default backup location
            event.setBackupLocation(generateBackupLocation(request.tenantId(), request.eventType()));
        }

        return eventRepository.save(event);
    }

    /**
     * Marks a DR event as completed.
     */
    @Transactional
    public DisasterRecoveryEvent completeEvent(Long id, Long fileSizeBytes) {
        DisasterRecoveryEvent event = getEvent(id);
        event.setStatus("COMPLETED");
        event.setCompletedAt(Instant.now());
        event.setFileSizeBytes(fileSizeBytes);
        return eventRepository.save(event);
    }

    /**
     * Marks a DR event as failed.
     */
    @Transactional
    public DisasterRecoveryEvent failEvent(Long id, String errorMessage) {
        DisasterRecoveryEvent event = getEvent(id);
        event.setStatus("FAILED");
        event.setCompletedAt(Instant.now());
        event.setErrorMessage(errorMessage);
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<DisasterRecoveryEvent> getEvents(String tenantId) {
        return eventRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public DisasterRecoveryEvent getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DR event not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<DisasterRecoveryEvent> getEventsByType(String tenantId, String eventType) {
        return eventRepository.findByTenantIdAndEventType(tenantId, eventType);
    }

    /**
     * Initiates a point-in-time recovery to a specific timestamp.
     */
    @Transactional
    public DisasterRecoveryEvent initiatePointInTimeRecovery(String tenantId, Instant targetTime) {
        DisasterRecoveryEvent event = new DisasterRecoveryEvent(tenantId, "POINT_IN_TIME_RECOVERY");
        event.setPointInTime(targetTime);
        event.setBackupLocation(generateBackupLocation(tenantId, "POINT_IN_TIME_RECOVERY"));
        return eventRepository.save(event);
    }

    private String generateBackupLocation(String tenantId, String eventType) {
        return String.format("s3://onebook-dr/%s/%s/%s",
                tenantId, eventType.toLowerCase(), UUID.randomUUID());
    }
}

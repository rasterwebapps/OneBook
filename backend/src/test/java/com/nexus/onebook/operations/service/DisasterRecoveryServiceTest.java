package com.nexus.onebook.operations.service;

import com.nexus.onebook.operations.dto.DisasterRecoveryRequest;
import com.nexus.onebook.operations.model.DisasterRecoveryEvent;
import com.nexus.onebook.operations.repository.DisasterRecoveryEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisasterRecoveryServiceTest {

    @Mock
    private DisasterRecoveryEventRepository eventRepository;

    @InjectMocks
    private DisasterRecoveryService disasterRecoveryService;

    @Test
    void initiateEvent_fullBackup_succeeds() {
        DisasterRecoveryRequest request = new DisasterRecoveryRequest(
                "tenant-1", "FULL_BACKUP", null);

        when(eventRepository.save(any(DisasterRecoveryEvent.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DisasterRecoveryEvent result = disasterRecoveryService.initiateEvent(request);

        assertNotNull(result);
        assertEquals("tenant-1", result.getTenantId());
        assertEquals("FULL_BACKUP", result.getEventType());
        assertEquals("STARTED", result.getStatus());
        assertNotNull(result.getBackupLocation());
        assertTrue(result.getBackupLocation().startsWith("s3://onebook-dr/tenant-1/"));
    }

    @Test
    void initiateEvent_withCustomLocation_usesProvidedLocation() {
        DisasterRecoveryRequest request = new DisasterRecoveryRequest(
                "tenant-1", "INCREMENTAL_BACKUP", "s3://custom-bucket/backup");

        when(eventRepository.save(any(DisasterRecoveryEvent.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DisasterRecoveryEvent result = disasterRecoveryService.initiateEvent(request);

        assertEquals("s3://custom-bucket/backup", result.getBackupLocation());
    }

    @Test
    void initiateEvent_invalidEventType_throws() {
        DisasterRecoveryRequest request = new DisasterRecoveryRequest(
                "tenant-1", "INVALID_TYPE", null);

        assertThrows(IllegalArgumentException.class,
                () -> disasterRecoveryService.initiateEvent(request));
    }

    @Test
    void completeEvent_setsCompletedStatus() {
        DisasterRecoveryEvent event = new DisasterRecoveryEvent("tenant-1", "FULL_BACKUP");
        event.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DisasterRecoveryEvent result = disasterRecoveryService.completeEvent(1L, 1024000L);

        assertEquals("COMPLETED", result.getStatus());
        assertEquals(1024000L, result.getFileSizeBytes());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    void failEvent_setsFailedStatus() {
        DisasterRecoveryEvent event = new DisasterRecoveryEvent("tenant-1", "FULL_BACKUP");
        event.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DisasterRecoveryEvent result = disasterRecoveryService.failEvent(1L, "Disk full");

        assertEquals("FAILED", result.getStatus());
        assertEquals("Disk full", result.getErrorMessage());
    }

    @Test
    void getEvent_notFound_throws() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> disasterRecoveryService.getEvent(99L));
    }

    @Test
    void initiatePointInTimeRecovery_succeeds() {
        Instant targetTime = Instant.parse("2024-06-15T10:00:00Z");

        when(eventRepository.save(any(DisasterRecoveryEvent.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DisasterRecoveryEvent result = disasterRecoveryService
                .initiatePointInTimeRecovery("tenant-1", targetTime);

        assertEquals("POINT_IN_TIME_RECOVERY", result.getEventType());
        assertEquals(targetTime, result.getPointInTime());
    }

    @Test
    void getEvents_returnsList() {
        when(eventRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(new DisasterRecoveryEvent()));

        List<DisasterRecoveryEvent> result = disasterRecoveryService.getEvents("tenant-1");

        assertEquals(1, result.size());
    }
}

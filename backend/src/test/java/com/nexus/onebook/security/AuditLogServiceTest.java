package com.nexus.onebook.security;

import com.nexus.onebook.security.model.AuditLogEntry;
import com.nexus.onebook.security.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        lenient().when(auditLogRepository.findLatestByTenantId(any())).thenReturn(Optional.empty());
        lenient().when(auditLogRepository.save(any(AuditLogEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void logInsert_createsEntryWithHash() {
        AuditLogEntry entry = auditLogService.logInsert(
                "tenant-1", "ledger_accounts", 1L, "{\"accountCode\":\"1000\"}");

        assertNotNull(entry);
        assertEquals("tenant-1", entry.getTenantId());
        assertEquals("ledger_accounts", entry.getTableName());
        assertEquals(1L, entry.getRecordId());
        assertEquals("INSERT", entry.getOperation());
        assertNotNull(entry.getHash());
        assertNull(entry.getPrevHash());
        assertNull(entry.getOldValues());
        assertEquals("{\"accountCode\":\"1000\"}", entry.getNewValues());
    }

    @Test
    void logUpdate_capturesOldAndNewValues() {
        AuditLogEntry entry = auditLogService.logUpdate(
                "tenant-1", "ledger_accounts", 1L,
                "{\"name\":\"Old\"}", "{\"name\":\"New\"}");

        assertEquals("UPDATE", entry.getOperation());
        assertEquals("{\"name\":\"Old\"}", entry.getOldValues());
        assertEquals("{\"name\":\"New\"}", entry.getNewValues());
    }

    @Test
    void logDelete_capturesOldValues() {
        AuditLogEntry entry = auditLogService.logDelete(
                "tenant-1", "ledger_accounts", 1L,
                "{\"accountCode\":\"1000\"}");

        assertEquals("DELETE", entry.getOperation());
        assertEquals("{\"accountCode\":\"1000\"}", entry.getOldValues());
        assertNull(entry.getNewValues());
    }

    @Test
    void hashChain_secondEntryLinksToPrevious() {
        // First entry: no previous hash
        AuditLogEntry first = auditLogService.logInsert(
                "tenant-1", "ledger_accounts", 1L, "{\"code\":\"1000\"}");

        // Set up repository to return the first entry as "latest"
        reset(auditLogRepository);
        when(auditLogRepository.findLatestByTenantId("tenant-1"))
                .thenReturn(Optional.of(first));
        when(auditLogRepository.save(any(AuditLogEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Second entry: should link to first
        AuditLogEntry second = auditLogService.logInsert(
                "tenant-1", "ledger_accounts", 2L, "{\"code\":\"2000\"}");

        assertEquals(first.getHash(), second.getPrevHash(),
                "Second entry's prevHash must equal first entry's hash");
    }

    @Test
    void verifyChain_intactChain_returnsTrue() {
        // Simulate a chain of 3 entries that is consistent
        AuditLogEntry e1 = makeEntry("tenant-1", "t", 1L, "INSERT", null, "{\"a\":1}");
        e1.setPrevHash(null);
        e1.setHash(auditLogService.computeHash(null, e1));

        AuditLogEntry e2 = makeEntry("tenant-1", "t", 2L, "INSERT", null, "{\"b\":2}");
        e2.setPrevHash(e1.getHash());
        e2.setHash(auditLogService.computeHash(e1.getHash(), e2));

        AuditLogEntry e3 = makeEntry("tenant-1", "t", 3L, "UPDATE", "{\"b\":2}", "{\"b\":3}");
        e3.setPrevHash(e2.getHash());
        e3.setHash(auditLogService.computeHash(e2.getHash(), e3));

        when(auditLogRepository.findByTenantIdOrderByIdAsc("tenant-1"))
                .thenReturn(List.of(e1, e2, e3));

        assertTrue(auditLogService.verifyChain("tenant-1"));
    }

    @Test
    void verifyChain_tamperedEntry_returnsFalse() {
        // Build a valid chain then tamper with the middle entry
        AuditLogEntry e1 = makeEntry("tenant-1", "t", 1L, "INSERT", null, "{\"a\":1}");
        e1.setPrevHash(null);
        e1.setHash(auditLogService.computeHash(null, e1));

        AuditLogEntry e2 = makeEntry("tenant-1", "t", 2L, "INSERT", null, "{\"b\":2}");
        e2.setPrevHash(e1.getHash());
        e2.setHash(auditLogService.computeHash(e1.getHash(), e2));

        // Tamper: change the new_values but keep the original hash
        e2.setNewValues("{\"b\":\"HACKED\"}");

        when(auditLogRepository.findByTenantIdOrderByIdAsc("tenant-1"))
                .thenReturn(List.of(e1, e2));

        assertFalse(auditLogService.verifyChain("tenant-1"),
                "Tampered entry should break the chain");
    }

    @Test
    void verifyChain_emptyChain_returnsTrue() {
        when(auditLogRepository.findByTenantIdOrderByIdAsc("tenant-1"))
                .thenReturn(List.of());

        assertTrue(auditLogService.verifyChain("tenant-1"));
    }

    @Test
    void verifyChainForRecord_intactChain_returnsTrue() {
        AuditLogEntry e1 = makeEntry("tenant-1", "ledger_accounts", 1L, "INSERT", null, "{\"a\":1}");
        e1.setPrevHash(null);
        e1.setHash(auditLogService.computeHash(null, e1));

        when(auditLogRepository.findByTenantIdAndTableNameAndRecordIdOrderByIdAsc(
                "tenant-1", "ledger_accounts", 1L))
                .thenReturn(List.of(e1));

        assertTrue(auditLogService.verifyChainForRecord("tenant-1", "ledger_accounts", 1L));
    }

    @Test
    void computeHash_deterministic() {
        AuditLogEntry entry = makeEntry("tenant-1", "t", 1L, "INSERT", null, "{\"a\":1}");
        String hash1 = auditLogService.computeHash(null, entry);
        String hash2 = auditLogService.computeHash(null, entry);
        assertEquals(hash1, hash2);
    }

    @Test
    void computeHash_is64HexChars() {
        AuditLogEntry entry = makeEntry("tenant-1", "t", 1L, "INSERT", null, "{\"a\":1}");
        String hash = auditLogService.computeHash(null, entry);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    // --- Helper ---

    private AuditLogEntry makeEntry(String tenantId, String tableName, Long recordId,
                                    String operation, String oldValues, String newValues) {
        AuditLogEntry e = new AuditLogEntry();
        e.setTenantId(tenantId);
        e.setTableName(tableName);
        e.setRecordId(recordId);
        e.setOperation(operation);
        e.setOldValues(oldValues);
        e.setNewValues(newValues);
        return e;
    }
}

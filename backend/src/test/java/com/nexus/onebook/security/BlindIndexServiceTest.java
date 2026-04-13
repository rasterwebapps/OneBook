package com.nexus.onebook.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlindIndexServiceTest {

    private BlindIndexService blindIndexService;

    @BeforeEach
    void setUp() {
        KeyManagementService keyMgmt = new KeyManagementService(
                "aa11bb22cc33dd44ee55ff6600112233aa11bb22cc33dd44ee55ff6600112233", 1);
        blindIndexService = new BlindIndexService(keyMgmt);
    }

    @Test
    void generateBlindIndex_deterministic() {
        String plaintext = "Pharmacy Revenue Account";
        String hash1 = blindIndexService.generateBlindIndex(plaintext);
        String hash2 = blindIndexService.generateBlindIndex(plaintext);

        assertEquals(hash1, hash2, "Same input must produce same blind index");
    }

    @Test
    void generateBlindIndex_differentInputsProduceDifferentHashes() {
        String hash1 = blindIndexService.generateBlindIndex("Cash");
        String hash2 = blindIndexService.generateBlindIndex("Revenue");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void generateBlindIndex_returnsHexString() {
        String hash = blindIndexService.generateBlindIndex("Test");

        // HMAC-SHA256 = 32 bytes = 64 hex chars
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"), "Should be lowercase hex");
    }

    @Test
    void generateBlindIndex_null_returnsNull() {
        assertNull(blindIndexService.generateBlindIndex(null));
    }

    @Test
    void generateBlindIndex_isIrreversible() {
        String hash = blindIndexService.generateBlindIndex("Secret Account Name");

        // The hash should not contain the original value
        assertFalse(hash.contains("Secret"));
        assertFalse(hash.contains("Account"));
    }

    @Test
    void generateBlindIndex_caseSensitive() {
        String hash1 = blindIndexService.generateBlindIndex("cash");
        String hash2 = blindIndexService.generateBlindIndex("Cash");

        assertNotEquals(hash1, hash2, "Blind index should be case-sensitive");
    }

    @Test
    void generateBlindIndex_emptyString_returnsHash() {
        String hash = blindIndexService.generateBlindIndex("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}

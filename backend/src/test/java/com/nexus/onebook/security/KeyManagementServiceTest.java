package com.nexus.onebook.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class KeyManagementServiceTest {

    private KeyManagementService keyManagementService;

    @BeforeEach
    void setUp() {
        // 64-char hex = 256-bit key
        keyManagementService = new KeyManagementService(
                "aa11bb22cc33dd44ee55ff6600112233aa11bb22cc33dd44ee55ff6600112233", 1);
    }

    @Test
    void getCurrentKeyVersion_returnsConfiguredVersion() {
        assertEquals(1, keyManagementService.getCurrentKeyVersion());
    }

    @Test
    void getEncryptionKey_returnsNonNullKey() {
        SecretKey key = keyManagementService.getEncryptionKey(1);
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
    }

    @Test
    void getEncryptionKey_sameVersionReturnsSameKey() {
        SecretKey key1 = keyManagementService.getEncryptionKey(1);
        SecretKey key2 = keyManagementService.getEncryptionKey(1);
        assertArrayEquals(key1.getEncoded(), key2.getEncoded());
    }

    @Test
    void getEncryptionKey_differentVersionsReturnDifferentKeys() {
        SecretKey key1 = keyManagementService.getEncryptionKey(1);
        SecretKey key2 = keyManagementService.getEncryptionKey(2);
        assertFalse(java.util.Arrays.equals(key1.getEncoded(), key2.getEncoded()));
    }

    @Test
    void getHmacKey_returnsNonNull32Bytes() {
        byte[] hmacKey = keyManagementService.getHmacKey(1);
        assertNotNull(hmacKey);
        assertEquals(32, hmacKey.length);
    }

    @Test
    void getHmacKey_differentFromEncryptionKey() {
        SecretKey encKey = keyManagementService.getEncryptionKey(1);
        byte[] hmacKey = keyManagementService.getHmacKey(1);
        assertFalse(java.util.Arrays.equals(encKey.getEncoded(), hmacKey));
    }

    @Test
    void hexToBytes_convertsCorrectly() {
        byte[] result = KeyManagementService.hexToBytes("aabb");
        assertEquals(2, result.length);
        assertEquals((byte) 0xAA, result[0]);
        assertEquals((byte) 0xBB, result[1]);
    }

    @Test
    void hexToBytes_oddLength_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> KeyManagementService.hexToBytes("abc"));
    }

    @Test
    void bytesToHex_convertsCorrectly() {
        byte[] input = { (byte) 0xAA, (byte) 0xBB, (byte) 0x00, (byte) 0xFF };
        assertEquals("aabb00ff", KeyManagementService.bytesToHex(input));
    }

    @Test
    void roundtrip_hexConversion() {
        String hex = "0123456789abcdef";
        assertEquals(hex, KeyManagementService.bytesToHex(KeyManagementService.hexToBytes(hex)));
    }
}

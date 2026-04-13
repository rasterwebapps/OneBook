package com.nexus.onebook.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldEncryptionServiceTest {

    private FieldEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        KeyManagementService keyMgmt = new KeyManagementService(
                "aa11bb22cc33dd44ee55ff6600112233aa11bb22cc33dd44ee55ff6600112233", 1);
        encryptionService = new FieldEncryptionService(keyMgmt);
    }

    @Test
    void encryptDecrypt_roundtrip_preservesPlaintext() {
        String plaintext = "Pharmacy Revenue Account";
        String ciphertext = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(ciphertext);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_producesNonPlaintextOutput() {
        String plaintext = "Pharmacy Revenue Account";
        String ciphertext = encryptionService.encrypt(plaintext);

        assertNotEquals(plaintext, ciphertext);
    }

    @Test
    void encrypt_sameInputProducesDifferentCiphertext() {
        // AES-GCM uses a random IV, so two encryptions of the same value differ
        String plaintext = "Cash Account";
        String ct1 = encryptionService.encrypt(plaintext);
        String ct2 = encryptionService.encrypt(plaintext);

        assertNotEquals(ct1, ct2, "Each encryption should use a unique IV");
    }

    @Test
    void encrypt_null_returnsNull() {
        assertNull(encryptionService.encrypt(null));
    }

    @Test
    void decrypt_null_returnsNull() {
        assertNull(encryptionService.decrypt(null));
    }

    @Test
    void encryptDecrypt_emptyString() {
        String ciphertext = encryptionService.encrypt("");
        assertEquals("", encryptionService.decrypt(ciphertext));
    }

    @Test
    void encryptDecrypt_unicodeCharacters() {
        String plaintext = "日本語テスト — Ñoño — 中文";
        String ciphertext = encryptionService.encrypt(plaintext);
        assertEquals(plaintext, encryptionService.decrypt(ciphertext));
    }

    @Test
    void encryptDecrypt_longString() {
        String plaintext = "A".repeat(10_000);
        String ciphertext = encryptionService.encrypt(plaintext);
        assertEquals(plaintext, encryptionService.decrypt(ciphertext));
    }

    @Test
    void decrypt_tamperedCiphertext_throws() {
        String ciphertext = encryptionService.encrypt("Test value");
        // Flip a character in the ciphertext to simulate tampering
        char[] chars = ciphertext.toCharArray();
        chars[chars.length / 2] = (chars[chars.length / 2] == 'A') ? 'B' : 'A';
        String tampered = new String(chars);

        assertThrows(IllegalStateException.class, () -> encryptionService.decrypt(tampered));
    }

    @Test
    void encryptWithVersion_andDecrypt_acrossVersions() {
        // Encrypt with version 1
        String plaintext = "Versioned Data";
        String ciphertextV1 = encryptionService.encryptWithVersion(plaintext, 1);

        // Decrypt should still work because version is embedded in ciphertext
        assertEquals(plaintext, encryptionService.decrypt(ciphertextV1));

        // Encrypt with version 2
        String ciphertextV2 = encryptionService.encryptWithVersion(plaintext, 2);
        assertEquals(plaintext, encryptionService.decrypt(ciphertextV2));
    }

    @Test
    void dbaCannotReadEncryptedValue() {
        // Simulate what a DBA would see in the database
        String sensitiveAccountName = "Patient Treatment Revenue";
        String storedValue = encryptionService.encrypt(sensitiveAccountName);

        // The stored value is Base64-encoded ciphertext — not the plaintext
        assertNotEquals(sensitiveAccountName, storedValue);
        assertFalse(storedValue.contains("Patient"));
        assertFalse(storedValue.contains("Treatment"));
        assertFalse(storedValue.contains("Revenue"));
    }
}

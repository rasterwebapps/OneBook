package com.nexus.onebook.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Provides selective field-level encryption using AES-256-GCM.
 * <p>
 * Each ciphertext is prefixed with the key version so that data encrypted
 * under older keys can still be decrypted after key rotation.
 * <p>
 * Wire format (Base64-encoded):
 * <pre>
 *   [1 byte version][12 bytes IV][N bytes ciphertext+tag]
 * </pre>
 */
@Service
public class FieldEncryptionService {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;    // 96 bits recommended by NIST
    private static final int GCM_TAG_LENGTH = 128;   // 128-bit authentication tag

    private final KeyManagementService keyManagementService;
    private final SecureRandom secureRandom = new SecureRandom();

    public FieldEncryptionService(KeyManagementService keyManagementService) {
        this.keyManagementService = keyManagementService;
    }

    /**
     * Encrypts plaintext using the current key version.
     *
     * @param plaintext the value to encrypt (may be null)
     * @return Base64-encoded ciphertext with version prefix, or null if input is null
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        return encryptWithVersion(plaintext, keyManagementService.getCurrentKeyVersion());
    }

    /**
     * Encrypts plaintext using a specific key version.
     */
    public String encryptWithVersion(String plaintext, int keyVersion) {
        if (plaintext == null) {
            return null;
        }
        try {
            final SecretKey key = keyManagementService.getEncryptionKey(keyVersion);
            final byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            final byte[] ciphertextBytes = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Pack: [version (1 byte)][IV (12 bytes)][ciphertext+tag]
            final ByteBuffer buffer = ByteBuffer.allocate(1 + GCM_IV_LENGTH + ciphertextBytes.length);
            buffer.put((byte) keyVersion);
            buffer.put(iv);
            buffer.put(ciphertextBytes);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64-encoded ciphertext that was produced by {@link #encrypt}.
     *
     * @param ciphertext the Base64 string to decrypt (may be null)
     * @return the original plaintext, or null if input is null
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            final byte[] decoded = Base64.getDecoder().decode(ciphertext);
            final ByteBuffer buffer = ByteBuffer.wrap(decoded);

            final int keyVersion = buffer.get() & 0xFF;
            final byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            final byte[] ciphertextBytes = new byte[buffer.remaining()];
            buffer.get(ciphertextBytes);

            final SecretKey key = keyManagementService.getEncryptionKey(keyVersion);
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            final byte[] plainBytes = cipher.doFinal(ciphertextBytes);
            return new String(plainBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}

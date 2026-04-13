package com.nexus.onebook.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cryptographic keys using envelope encryption.
 * <p>
 * The master key (from configuration) is used to derive purpose-specific
 * data keys (one for AES encryption, one for HMAC blind indexing).
 * Key versioning enables rotation without re-encrypting all existing data.
 */
@Service
public class KeyManagementService {

    private static final String AES_ALGORITHM = "AES";
    private static final int AES_KEY_LENGTH_BYTES = 32; // 256 bits

    private final byte[] masterKey;
    private final int currentKeyVersion;
    private final Map<Integer, SecretKey> encryptionKeyCache = new ConcurrentHashMap<>();
    private final Map<Integer, byte[]> hmacKeyCache = new ConcurrentHashMap<>();

    public KeyManagementService(
            @Value("${onebook.security.encryption.master-key}") String masterKeyHex,
            @Value("${onebook.security.encryption.key-version:1}") int keyVersion) {
        this.masterKey = hexToBytes(masterKeyHex);
        this.currentKeyVersion = keyVersion;
    }

    /**
     * Returns the current key version number.
     */
    public int getCurrentKeyVersion() {
        return currentKeyVersion;
    }

    /**
     * Derives an AES-256 encryption key for the given version using
     * HKDF-style derivation: SHA-256(masterKey || "enc" || version).
     */
    public SecretKey getEncryptionKey(int version) {
        return encryptionKeyCache.computeIfAbsent(version, v -> {
            final byte[] derived = deriveKey("enc", v);
            return new SecretKeySpec(derived, AES_ALGORITHM);
        });
    }

    /**
     * Returns the AES encryption key for the current version.
     */
    public SecretKey getCurrentEncryptionKey() {
        return getEncryptionKey(currentKeyVersion);
    }

    /**
     * Derives an HMAC key for blind indexing at the given version.
     */
    public byte[] getHmacKey(int version) {
        return hmacKeyCache.computeIfAbsent(version, v -> deriveKey("hmac", v));
    }

    /**
     * Returns the HMAC key for the current version.
     */
    public byte[] getCurrentHmacKey() {
        return getHmacKey(currentKeyVersion);
    }

    /**
     * Simple key derivation: SHA-256(masterKey || purpose || version).
     * Produces a 32-byte (256-bit) derived key.
     */
    private byte[] deriveKey(String purpose, int version) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(masterKey);
            digest.update(purpose.getBytes(StandardCharsets.UTF_8));
            digest.update(String.valueOf(version).getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Converts a hex-encoded string to a byte array.
     */
    static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        final byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    /**
     * Converts a byte array to a lowercase hex-encoded string.
     */
    static String bytesToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }
}

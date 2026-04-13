package com.nexus.onebook.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Generates deterministic blind indexes using HMAC-SHA256.
 * <p>
 * A blind index allows fast equality searches on encrypted fields
 * without exposing the plaintext to the database or DBA. The same
 * plaintext always produces the same index value, but the index
 * cannot be reversed to recover the original value.
 */
@Service
public class BlindIndexService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final KeyManagementService keyManagementService;

    public BlindIndexService(KeyManagementService keyManagementService) {
        this.keyManagementService = keyManagementService;
    }

    /**
     * Generates a hex-encoded HMAC-SHA256 blind index for the given plaintext.
     *
     * @param plaintext the value to index (may be null)
     * @return hex-encoded blind index, or null if input is null
     */
    public String generateBlindIndex(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            final byte[] hmacKey = keyManagementService.getCurrentHmacKey();
            final Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(hmacKey, HMAC_ALGORITHM));
            final byte[] hash = mac.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return KeyManagementService.bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }
}

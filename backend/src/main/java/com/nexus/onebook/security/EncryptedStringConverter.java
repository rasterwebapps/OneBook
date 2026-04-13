package com.nexus.onebook.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * JPA {@link AttributeConverter} that transparently encrypts field values
 * on write and decrypts on read using AES-256-GCM.
 * <p>
 * The static field pattern is necessary because JPA instantiates converters
 * via the no-arg constructor and does not support dependency injection.
 * Spring sets the static reference once at startup via the parameterized
 * constructor. The {@link FieldEncryptionService} is stateless and
 * thread-safe, so sharing a single instance across converter instances
 * is safe.
 * <p>
 * Apply to any entity column that must be stored encrypted at rest:
 * <pre>
 *   &#064;Convert(converter = EncryptedStringConverter.class)
 *   private String accountName;
 * </pre>
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static volatile FieldEncryptionService encryptionService;

    /**
     * Spring injects the {@link FieldEncryptionService} via this constructor.
     * The static reference is set once at startup and is safe for concurrent access
     * because {@link FieldEncryptionService} is stateless and thread-safe.
     */
    public EncryptedStringConverter(FieldEncryptionService fieldEncryptionService) {
        EncryptedStringConverter.encryptionService = fieldEncryptionService;
    }

    /** No-arg constructor required by JPA. */
    public EncryptedStringConverter() {}

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (encryptionService == null || attribute == null) {
            return attribute;
        }
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (encryptionService == null || dbData == null) {
            return dbData;
        }
        return encryptionService.decrypt(dbData);
    }
}

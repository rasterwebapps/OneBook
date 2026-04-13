package com.nexus.onebook.ingestion.model;

/**
 * Enumerates the supported external system adapter types.
 * Each type corresponds to an industry protocol or integration channel.
 * <p>
 * Use {@code EXTERNAL_APP} for any integrated business application
 * (Pharmacy, Lab, Stores, HIS, ERP, etc.) that sends payment requests using the
 * common {@code ExternalAppPaymentRequest} format. The {@code applicationName}
 * field inside the payload identifies the specific source system at runtime.
 */
public enum AdapterType {
    HL7,
    DMS,
    ISO_20022,
    REST_WEBHOOK,
    CORPORATE_CARD,
    HRM_PAYROLL,
    INVENTORY,
    EXTERNAL_APP
}

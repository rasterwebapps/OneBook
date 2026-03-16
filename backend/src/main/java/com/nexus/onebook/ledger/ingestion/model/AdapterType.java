package com.nexus.onebook.ledger.ingestion.model;

/**
 * Enumerates the supported external system adapter types.
 * Each type corresponds to an industry protocol or integration channel.
 */
public enum AdapterType {
    HL7,
    DMS,
    ISO_20022,
    REST_WEBHOOK,
    CORPORATE_CARD,
    HRM_PAYROLL,
    INVENTORY,
    PHARMACY
}

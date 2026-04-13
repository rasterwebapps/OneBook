package com.nexus.onebook.ledger.operations.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * WhatsApp Integration service — share invoices, ledger statements,
 * and reports with customers via WhatsApp Business API.
 * This is a pluggable integration point for WhatsApp Business API providers.
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    /**
     * Sends a document (invoice/statement) via WhatsApp.
     *
     * @param phoneNumber recipient phone number in international format
     * @param documentType type of document (INVOICE, STATEMENT, REPORT)
     * @param documentId internal document ID
     * @return delivery status
     */
    public Map<String, String> sendDocument(String phoneNumber, String documentType, String documentId) {
        log.info("WhatsApp send request — phone: {}, type: {}, docId: {}",
                phoneNumber, documentType, documentId);

        // Integration point for WhatsApp Business API
        // In production, this would call the WhatsApp Business API provider
        return Map.of(
                "status", "QUEUED",
                "phoneNumber", phoneNumber,
                "documentType", documentType,
                "documentId", documentId,
                "message", "Document queued for WhatsApp delivery"
        );
    }

    /**
     * Sends a text message via WhatsApp.
     */
    public Map<String, String> sendMessage(String phoneNumber, String message) {
        log.info("WhatsApp message request — phone: {}", phoneNumber);

        return Map.of(
                "status", "QUEUED",
                "phoneNumber", phoneNumber,
                "message", "Message queued for WhatsApp delivery"
        );
    }
}

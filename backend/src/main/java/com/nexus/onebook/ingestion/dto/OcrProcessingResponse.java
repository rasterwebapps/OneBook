package com.nexus.onebook.ingestion.dto;

/**
 * Response DTO returned after OCR processing of a vault document.
 */
public record OcrProcessingResponse(
        String documentId,
        String ocrStatus,
        String extractedData,
        String message
) {}

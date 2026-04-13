package com.nexus.onebook.reporting.controller;

import com.nexus.onebook.reporting.service.ExportService;
import com.nexus.onebook.operations.service.WhatsAppService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for data export (JSON/CSV) and WhatsApp integration.
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;
    private final WhatsAppService whatsAppService;

    public ExportController(ExportService exportService, WhatsAppService whatsAppService) {
        this.exportService = exportService;
        this.whatsAppService = whatsAppService;
    }

    @GetMapping("/trial-balance")
    public ResponseEntity<Map<String, Object>> exportTrialBalance(@RequestParam String tenantId) {
        return ResponseEntity.ok(exportService.exportTrialBalanceAsJson(tenantId));
    }

    @GetMapping("/ledger-accounts/json")
    public ResponseEntity<List<Map<String, Object>>> exportLedgerJson(@RequestParam String tenantId) {
        return ResponseEntity.ok(exportService.exportLedgerAccountsAsJson(tenantId));
    }

    @GetMapping("/ledger-accounts/csv")
    public ResponseEntity<String> exportLedgerCsv(@RequestParam String tenantId) {
        String csv = exportService.exportLedgerAccountsAsCsv(tenantId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ledger_accounts.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/whatsapp/send-document")
    public ResponseEntity<Map<String, String>> sendWhatsAppDocument(
            @RequestParam String phoneNumber,
            @RequestParam String documentType,
            @RequestParam String documentId) {
        return ResponseEntity.ok(whatsAppService.sendDocument(phoneNumber, documentType, documentId));
    }

    @PostMapping("/whatsapp/send-message")
    public ResponseEntity<Map<String, String>> sendWhatsAppMessage(
            @RequestParam String phoneNumber,
            @RequestParam String message) {
        return ResponseEntity.ok(whatsAppService.sendMessage(phoneNumber, message));
    }
}

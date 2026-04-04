package com.nexus.onebook.ledger.controller;

import com.nexus.onebook.ledger.dto.ClientAccountRequest;
import com.nexus.onebook.ledger.dto.ClientAccountResponse;
import com.nexus.onebook.ledger.service.ClientAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Client Account management — CRUD for
 * customer, vendor, employee, and intercompany party accounts.
 */
@RestController
@RequestMapping("/api/client-accounts")
public class ClientAccountController {

    private final ClientAccountService clientAccountService;

    public ClientAccountController(ClientAccountService clientAccountService) {
        this.clientAccountService = clientAccountService;
    }

    @PostMapping
    public ResponseEntity<ClientAccountResponse> create(
            @Valid @RequestBody ClientAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientAccountService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ClientAccountResponse>> list(
            @RequestParam String tenantId,
            @RequestParam(required = false) String type) {
        if (type != null && !type.isBlank()) {
            return ResponseEntity.ok(clientAccountService.listByType(tenantId, type));
        }
        return ResponseEntity.ok(clientAccountService.list(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientAccountResponse> getById(
            @RequestParam String tenantId,
            @PathVariable Long id) {
        return ResponseEntity.ok(clientAccountService.getById(tenantId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientAccountResponse> update(
            @RequestParam String tenantId,
            @PathVariable Long id,
            @Valid @RequestBody ClientAccountRequest request) {
        return ResponseEntity.ok(clientAccountService.update(tenantId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @RequestParam String tenantId,
            @PathVariable Long id) {
        clientAccountService.deactivate(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}

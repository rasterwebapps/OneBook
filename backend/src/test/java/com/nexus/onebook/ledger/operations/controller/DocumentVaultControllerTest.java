package com.nexus.onebook.ledger.operations.controller;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.operations.model.VaultDocument;
import com.nexus.onebook.ledger.operations.service.DocumentVaultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentVaultController.class)
@Import(GlobalExceptionHandler.class)
class DocumentVaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentVaultService documentVaultService;

    @Test
    void uploadDocument_returns201() throws Exception {
        VaultDocument doc = new VaultDocument(
                "tenant-1", "invoice.pdf", "application/pdf",
                1024L, "vault/tenant-1/key", "abc123");

        when(documentVaultService.storeDocument(any())).thenReturn(doc);

        mockMvc.perform(post("/api/document-vault/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "fileName": "invoice.pdf",
                                    "contentType": "application/pdf",
                                    "fileSize": 1024,
                                    "checksum": "abc123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("invoice.pdf"));
    }

    @Test
    void getDocuments_returnsList() throws Exception {
        when(documentVaultService.getDocuments("tenant-1"))
                .thenReturn(List.of(new VaultDocument()));

        mockMvc.perform(get("/api/document-vault/documents")
                        .param("tenantId", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deleteDocument_returns204() throws Exception {
        mockMvc.perform(delete("/api/document-vault/documents/1"))
                .andExpect(status().isNoContent());
    }
}

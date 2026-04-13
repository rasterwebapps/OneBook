package com.nexus.onebook.accounts.controller;

import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.exception.UnbalancedTransactionException;
import com.nexus.onebook.accounts.model.JournalTransaction;
import com.nexus.onebook.accounts.service.JournalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JournalController.class)
@Import(GlobalExceptionHandler.class)
class JournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JournalService journalService;


    @Test
    void createTransaction_validRequest_returns201() throws Exception {
        JournalTransaction transaction = new JournalTransaction(
                "tenant-1", LocalDate.of(2026, 3, 9), "Test");

        when(journalService.createTransaction(any())).thenReturn(transaction);

        mockMvc.perform(post("/api/journal/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "transactionDate": "2026-03-09",
                                    "description": "Test transaction",
                                    "entries": [
                                        {
                                            "accountId": 1,
                                            "entryType": "DEBIT",
                                            "amount": 100.0000,
                                            "description": "Debit line"
                                        },
                                        {
                                            "accountId": 2,
                                            "entryType": "CREDIT",
                                            "amount": 100.0000,
                                            "description": "Credit line"
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.transactionUuid").exists());
    }

    @Test
    void createTransaction_unbalanced_returns400() throws Exception {
        when(journalService.createTransaction(any()))
                .thenThrow(new UnbalancedTransactionException("Transaction is unbalanced"));

        mockMvc.perform(post("/api/journal/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "transactionDate": "2026-03-09",
                                    "description": "Unbalanced",
                                    "entries": [
                                        {
                                            "accountId": 1,
                                            "entryType": "DEBIT",
                                            "amount": 100.0000,
                                            "description": "Debit"
                                        },
                                        {
                                            "accountId": 2,
                                            "entryType": "CREDIT",
                                            "amount": 50.0000,
                                            "description": "Credit"
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transaction is unbalanced"));
    }

    @Test
    void createTransaction_missingEntries_returns400() throws Exception {
        mockMvc.perform(post("/api/journal/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "transactionDate": "2026-03-09",
                                    "entries": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransaction_found_returns200() throws Exception {
        UUID uuid = UUID.randomUUID();
        JournalTransaction transaction = new JournalTransaction(
                "tenant-1", LocalDate.of(2026, 3, 9), "Test");
        transaction.setTransactionUuid(uuid);

        when(journalService.getTransactionByUuid(uuid))
                .thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/journal/transactions/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"));
    }

    @Test
    void getTransaction_notFound_returns400() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(journalService.getTransactionByUuid(uuid))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/journal/transactions/{uuid}", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void postTransaction_unposted_returns200() throws Exception {
        UUID uuid = UUID.randomUUID();
        JournalTransaction transaction = new JournalTransaction(
                "tenant-1", LocalDate.of(2026, 3, 9), "Test");
        transaction.setTransactionUuid(uuid);
        transaction.setPosted(true);

        when(journalService.postTransaction(uuid)).thenReturn(transaction);

        mockMvc.perform(post("/api/journal/transactions/{uuid}/post", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posted").value(true));
    }

    @Test
    void postTransaction_notFound_returns400() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(journalService.postTransaction(uuid))
                .thenThrow(new IllegalArgumentException("Transaction not found: " + uuid));

        mockMvc.perform(post("/api/journal/transactions/{uuid}/post", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void postTransaction_unbalanced_returns400() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(journalService.postTransaction(uuid))
                .thenThrow(new UnbalancedTransactionException("Transaction is unbalanced"));

        mockMvc.perform(post("/api/journal/transactions/{uuid}/post", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transaction is unbalanced"));
    }
}

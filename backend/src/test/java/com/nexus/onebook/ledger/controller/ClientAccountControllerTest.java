package com.nexus.onebook.ledger.controller;

import com.nexus.onebook.ledger.dto.ClientAccountResponse;
import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.service.ClientAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientAccountController.class)
@Import(GlobalExceptionHandler.class)
class ClientAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientAccountService clientAccountService;

    private ClientAccountResponse sampleResponse() {
        return new ClientAccountResponse(
                1L, "t1", 100L, "Sundry Debtors",
                "CUSTOMER", "Acme Corp", "John Doe",
                "john@acme.com", "9876543210",
                "123 Main St", "456 Ship St",
                "22AAAAA0000A1Z5", "AAAAA0000A",
                new BigDecimal("100000.0000"), 30,
                true, Instant.now(), Instant.now()
        );
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(clientAccountService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/client-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "ledgerAccountId": 100,
                                    "clientType": "CUSTOMER",
                                    "clientName": "Acme Corp",
                                    "contactPerson": "John Doe",
                                    "email": "john@acme.com",
                                    "phone": "9876543210",
                                    "billingAddress": "123 Main St",
                                    "shippingAddress": "456 Ship St",
                                    "gstin": "22AAAAA0000A1Z5",
                                    "pan": "AAAAA0000A",
                                    "creditLimit": 100000,
                                    "paymentTermsDays": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientName").value("Acme Corp"))
                .andExpect(jsonPath("$.clientType").value("CUSTOMER"))
                .andExpect(jsonPath("$.email").value("john@acme.com"));
    }

    @Test
    void create_missingRequiredField_returns400() throws Exception {
        mockMvc.perform(post("/api/client-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "ledgerAccountId": 100,
                                    "clientType": "CUSTOMER"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_noTypeFilter_returnsAll() throws Exception {
        when(clientAccountService.list("t1")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/client-accounts")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Acme Corp"));
    }

    @Test
    void list_withTypeFilter_returnsFiltered() throws Exception {
        when(clientAccountService.listByType("t1", "VENDOR")).thenReturn(List.of());

        mockMvc.perform(get("/api/client-accounts")
                        .param("tenantId", "t1")
                        .param("type", "VENDOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_exists_returnsOk() throws Exception {
        when(clientAccountService.getById("t1", 1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/client-accounts/1")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientName").value("Acme Corp"));
    }

    @Test
    void getById_notFound_returns400() throws Exception {
        when(clientAccountService.getById("t1", 99L))
                .thenThrow(new IllegalArgumentException("Client account not found: 99"));

        mockMvc.perform(get("/api/client-accounts/99")
                        .param("tenantId", "t1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Client account not found: 99"));
    }

    @Test
    void update_validRequest_returnsOk() throws Exception {
        ClientAccountResponse updated = new ClientAccountResponse(
                1L, "t1", 100L, "Sundry Debtors",
                "VENDOR", "Acme Updated", "Jane",
                "jane@acme.com", "1111111111",
                "New Addr", null, null, null,
                new BigDecimal("200000.0000"), 60,
                true, Instant.now(), Instant.now()
        );
        when(clientAccountService.update(eq("t1"), eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/client-accounts/1")
                        .param("tenantId", "t1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "ledgerAccountId": 100,
                                    "clientType": "VENDOR",
                                    "clientName": "Acme Updated",
                                    "contactPerson": "Jane",
                                    "email": "jane@acme.com",
                                    "phone": "1111111111",
                                    "billingAddress": "New Addr",
                                    "creditLimit": 200000,
                                    "paymentTermsDays": 60
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Acme Updated"))
                .andExpect(jsonPath("$.clientType").value("VENDOR"));
    }

    @Test
    void deactivate_exists_returns204() throws Exception {
        doNothing().when(clientAccountService).deactivate("t1", 1L);

        mockMvc.perform(delete("/api/client-accounts/1")
                        .param("tenantId", "t1"))
                .andExpect(status().isNoContent());

        verify(clientAccountService).deactivate("t1", 1L);
    }

    @Test
    void deactivate_notFound_returns400() throws Exception {
        doThrow(new IllegalArgumentException("Client account not found: 99"))
                .when(clientAccountService).deactivate("t1", 99L);

        mockMvc.perform(delete("/api/client-accounts/99")
                        .param("tenantId", "t1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Client account not found: 99"));
    }
}

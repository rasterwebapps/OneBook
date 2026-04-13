package com.nexus.onebook.ledger.banking.controller;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.banking.model.ChequeEntry;
import com.nexus.onebook.ledger.banking.model.ChequeStatus;
import com.nexus.onebook.ledger.banking.service.ChequeManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChequeController.class)
@Import(GlobalExceptionHandler.class)
class ChequeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChequeManagementService chequeService;

    @Test
    void issueCheque_validRequest_returns201() throws Exception {
        ChequeEntry entry = new ChequeEntry();
        entry.setId(1L);
        entry.setTenantId("t1");
        entry.setChequeNumber("CHQ001");
        entry.setStatus(ChequeStatus.ISSUED);

        when(chequeService.issueCheque(any())).thenReturn(entry);

        mockMvc.perform(post("/api/cheques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "chequeNumber": "CHQ001",
                                    "bankAccountId": 1,
                                    "partyName": "Supplier A",
                                    "amount": 50000,
                                    "chequeDate": "2024-01-15",
                                    "chequeType": "PAYMENT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chequeNumber").value("CHQ001"))
                .andExpect(jsonPath("$.status").value("ISSUED"));
    }

    @Test
    void getRegister_returnsList() throws Exception {
        ChequeEntry entry = new ChequeEntry();
        entry.setId(1L);
        entry.setTenantId("t1");
        entry.setChequeNumber("CHQ001");

        when(chequeService.getChequeRegister("t1")).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/cheques")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chequeNumber").value("CHQ001"));
    }

    @Test
    void clearCheque_validRequest_returns200() throws Exception {
        ChequeEntry entry = new ChequeEntry();
        entry.setId(1L);
        entry.setChequeNumber("CHQ001");
        entry.setStatus(ChequeStatus.CLEARED);

        when(chequeService.clearCheque(eq(1L), any(LocalDate.class))).thenReturn(entry);

        mockMvc.perform(post("/api/cheques/1/clear")
                        .param("clearingDate", "2024-01-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLEARED"));
    }

    @Test
    void bounceCheque_validRequest_returns200() throws Exception {
        ChequeEntry entry = new ChequeEntry();
        entry.setId(1L);
        entry.setChequeNumber("CHQ001");
        entry.setStatus(ChequeStatus.BOUNCED);

        when(chequeService.bounceCheque(eq(1L), eq("Insufficient funds"))).thenReturn(entry);

        mockMvc.perform(post("/api/cheques/1/bounce")
                        .param("reason", "Insufficient funds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BOUNCED"));
    }

    @Test
    void cancelCheque_validRequest_returns200() throws Exception {
        ChequeEntry entry = new ChequeEntry();
        entry.setId(1L);
        entry.setChequeNumber("CHQ001");
        entry.setStatus(ChequeStatus.CANCELLED);

        when(chequeService.cancelCheque(1L)).thenReturn(entry);

        mockMvc.perform(post("/api/cheques/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}

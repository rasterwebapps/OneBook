package com.nexus.onebook.ledger.banking.controller;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.banking.model.ConnectedPayment;
import com.nexus.onebook.ledger.banking.model.PaymentStatus;
import com.nexus.onebook.ledger.banking.service.ConnectedPaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConnectedPaymentService paymentService;

    @Test
    void initiatePayment_validRequest_returns201() throws Exception {
        ConnectedPayment payment = new ConnectedPayment();
        payment.setId(1L);
        payment.setTenantId("t1");
        payment.setBeneficiaryName("Vendor A");
        payment.setStatus(PaymentStatus.INITIATED);

        when(paymentService.initiatePayment(any())).thenReturn(payment);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "bankAccountId": 1,
                                    "beneficiaryName": "Vendor A",
                                    "beneficiaryAccount": "1234567890",
                                    "amount": 50000,
                                    "paymentMode": "NEFT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.beneficiaryName").value("Vendor A"))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }

    @Test
    void getPayments_returnsList() throws Exception {
        ConnectedPayment payment = new ConnectedPayment();
        payment.setId(1L);
        payment.setTenantId("t1");
        payment.setBeneficiaryName("Vendor A");

        when(paymentService.getPayments("t1")).thenReturn(List.of(payment));

        mockMvc.perform(get("/api/payments")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].beneficiaryName").value("Vendor A"));
    }

    @Test
    void completePayment_validRequest_returns200() throws Exception {
        ConnectedPayment payment = new ConnectedPayment();
        payment.setId(1L);
        payment.setBeneficiaryName("Vendor A");
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentService.completePayment(eq(1L), eq("REF001"))).thenReturn(payment);

        mockMvc.perform(post("/api/payments/1/complete")
                        .param("referenceNumber", "REF001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void failPayment_validRequest_returns200() throws Exception {
        ConnectedPayment payment = new ConnectedPayment();
        payment.setId(1L);
        payment.setBeneficiaryName("Vendor A");
        payment.setStatus(PaymentStatus.FAILED);

        when(paymentService.failPayment(eq(1L), eq("Insufficient balance"))).thenReturn(payment);

        mockMvc.perform(post("/api/payments/1/fail")
                        .param("reason", "Insufficient balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }
}

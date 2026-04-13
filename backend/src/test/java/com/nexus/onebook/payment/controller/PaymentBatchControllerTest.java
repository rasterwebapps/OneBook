package com.nexus.onebook.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.onebook.payment.dto.BatchApprovalRequest;
import com.nexus.onebook.payment.dto.CreateBatchRequest;
import com.nexus.onebook.payment.dto.PaymentBatchResponse;
import com.nexus.onebook.payment.service.PaymentBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentBatchControllerTest {

    @Mock
    private PaymentBatchService batchService;

    @InjectMocks
    private PaymentBatchController batchController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(batchController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private PaymentBatchResponse buildBatchResponse(Long id, String status) {
        return new PaymentBatchResponse(
            id, "tenant-1", "PB-2026-01-001", 10L, "Vendor A",
            new BigDecimal("1000.00"), new BigDecimal("100.00"), new BigDecimal("50.00"),
            new BigDecimal("850.00"), 20L, "NEFT",
            status, "user1", null, null, null, null, null, null, false,
            Instant.now(), Instant.now(), List.of()
        );
    }

    @Test
    void createBatch_returns201() throws Exception {
        CreateBatchRequest request = new CreateBatchRequest(10L, List.of(1L, 2L), 20L, "NEFT");
        PaymentBatchResponse response = buildBatchResponse(1L, "PENDING_APPROVAL");

        when(batchService.createBatch(eq("tenant-1"), any(CreateBatchRequest.class), eq("user1")))
            .thenReturn(response);

        mockMvc.perform(post("/api/payment-batches")
                .param("tenantId", "tenant-1")
                .param("createdBy", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.batchNumber").value("PB-2026-01-001"));
    }

    @Test
    void approveBatch_returns200() throws Exception {
        BatchApprovalRequest request = new BatchApprovalRequest("APPROVE", null);
        PaymentBatchResponse response = buildBatchResponse(1L, "APPROVED");

        when(batchService.approveBatch(eq("tenant-1"), eq(1L), anyString()))
            .thenReturn(response);

        mockMvc.perform(post("/api/payment-batches/1/approve")
                .param("tenantId", "tenant-1")
                .param("actorId", "approver1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void rejectBatch_returns200() throws Exception {
        BatchApprovalRequest request = new BatchApprovalRequest("REJECT", "Duplicate entry");
        PaymentBatchResponse response = buildBatchResponse(1L, "REJECTED");

        when(batchService.rejectBatch(eq("tenant-1"), eq(1L), anyString(), anyString()))
            .thenReturn(response);

        mockMvc.perform(post("/api/payment-batches/1/approve")
                .param("tenantId", "tenant-1")
                .param("actorId", "approver1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void generateFile_returns200() throws Exception {
        byte[] csvBytes = "Sr No,Vendor Name\n1,Vendor A\n".getBytes();

        when(batchService.generatePaymentFile(eq("tenant-1"), eq(1L))).thenReturn(csvBytes);

        mockMvc.perform(get("/api/payment-batches/1/generate-file")
                .param("tenantId", "tenant-1"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=payment-batch-1.csv"));
    }
}

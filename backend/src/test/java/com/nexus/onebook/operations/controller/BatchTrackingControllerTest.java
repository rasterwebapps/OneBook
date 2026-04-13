package com.nexus.onebook.operations.controller;

import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.operations.model.BatchStatus;
import com.nexus.onebook.operations.model.BatchTracking;
import com.nexus.onebook.operations.service.BatchTrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchTrackingController.class)
@Import(GlobalExceptionHandler.class)
class BatchTrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatchTrackingService batchTrackingService;

    @Test
    void createBatch_validRequest_returns201() throws Exception {
        BatchTracking batch = new BatchTracking();
        batch.setId(1L);
        batch.setTenantId("t1");
        batch.setBatchNumber("BATCH001");
        batch.setQuantity(new BigDecimal("100"));

        when(batchTrackingService.createBatch(any())).thenReturn(batch);

        mockMvc.perform(post("/api/batch-tracking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "stockItemId": 1,
                                    "batchNumber": "BATCH001",
                                    "quantity": 100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batchNumber").value("BATCH001"))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    void getBatches_returnsList() throws Exception {
        BatchTracking batch = new BatchTracking();
        batch.setId(1L);
        batch.setTenantId("t1");
        batch.setBatchNumber("BATCH001");

        when(batchTrackingService.getBatches("t1")).thenReturn(List.of(batch));

        mockMvc.perform(get("/api/batch-tracking")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batchNumber").value("BATCH001"));
    }

    @Test
    void getExpiringBatches_returnsList() throws Exception {
        BatchTracking batch = new BatchTracking();
        batch.setId(1L);
        batch.setTenantId("t1");
        batch.setBatchNumber("BATCH001");
        batch.setExpiryDate(LocalDate.of(2024, 6, 1));

        when(batchTrackingService.getExpiringBatches("t1", LocalDate.of(2024, 12, 31)))
                .thenReturn(List.of(batch));

        mockMvc.perform(get("/api/batch-tracking/expiring")
                        .param("tenantId", "t1")
                        .param("beforeDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batchNumber").value("BATCH001"));
    }

    @Test
    void markExpired_returnsOk() throws Exception {
        BatchTracking batch = new BatchTracking();
        batch.setId(1L);
        batch.setTenantId("t1");
        batch.setBatchNumber("BATCH001");
        batch.setStatus(BatchStatus.EXPIRED);

        when(batchTrackingService.markExpired(1L)).thenReturn(batch);

        mockMvc.perform(post("/api/batch-tracking/1/expire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
    }
}

package com.nexus.onebook.operations.controller;

import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.operations.model.DisasterRecoveryEvent;
import com.nexus.onebook.operations.service.DisasterRecoveryService;
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

@WebMvcTest(DisasterRecoveryController.class)
@Import(GlobalExceptionHandler.class)
class DisasterRecoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisasterRecoveryService disasterRecoveryService;

    @Test
    void initiateEvent_returns201() throws Exception {
        DisasterRecoveryEvent event = new DisasterRecoveryEvent("tenant-1", "FULL_BACKUP");

        when(disasterRecoveryService.initiateEvent(any())).thenReturn(event);

        mockMvc.perform(post("/api/disaster-recovery/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "eventType": "FULL_BACKUP"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("FULL_BACKUP"));
    }

    @Test
    void getEvents_returnsList() throws Exception {
        when(disasterRecoveryService.getEvents("tenant-1"))
                .thenReturn(List.of(new DisasterRecoveryEvent()));

        mockMvc.perform(get("/api/disaster-recovery/events")
                        .param("tenantId", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

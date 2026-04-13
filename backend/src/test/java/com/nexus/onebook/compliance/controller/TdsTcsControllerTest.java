package com.nexus.onebook.compliance.controller;

import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.compliance.model.TdsTcsEntry;
import com.nexus.onebook.compliance.model.TdsTcsStatus;
import com.nexus.onebook.compliance.model.TdsTcsType;
import com.nexus.onebook.compliance.service.TdsTcsService;
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

@WebMvcTest(TdsTcsController.class)
@Import(GlobalExceptionHandler.class)
class TdsTcsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TdsTcsService tdsTcsService;

    @Test
    void createEntry_validRequest_returns201() throws Exception {
        TdsTcsEntry entry = new TdsTcsEntry();
        entry.setId(1L);
        entry.setTenantId("t1");
        entry.setEntryType(TdsTcsType.TDS);
        entry.setSectionCode("194C");
        entry.setPartyName("Vendor A");

        when(tdsTcsService.createEntry(any())).thenReturn(entry);

        mockMvc.perform(post("/api/tds-tcs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "t1",
                                    "entryType": "TDS",
                                    "sectionCode": "194C",
                                    "partyName": "Vendor A",
                                    "transactionDate": "2024-01-15",
                                    "taxableAmount": 100000,
                                    "taxRate": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sectionCode").value("194C"))
                .andExpect(jsonPath("$.partyName").value("Vendor A"));
    }

    @Test
    void getEntries_returnsList() throws Exception {
        TdsTcsEntry entry = new TdsTcsEntry();
        entry.setId(1L);
        entry.setTenantId("t1");
        entry.setEntryType(TdsTcsType.TDS);
        entry.setSectionCode("194C");

        when(tdsTcsService.getEntries("t1")).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/tds-tcs")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectionCode").value("194C"));
    }

    @Test
    void getPending_returnsList() throws Exception {
        TdsTcsEntry entry = new TdsTcsEntry();
        entry.setId(1L);
        entry.setTenantId("t1");
        entry.setEntryType(TdsTcsType.TCS);

        when(tdsTcsService.getPendingEntries("t1")).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/tds-tcs/pending")
                        .param("tenantId", "t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entryType").value("TCS"));
    }

    @Test
    void updateStatus_validRequest_returns200() throws Exception {
        TdsTcsEntry entry = new TdsTcsEntry();
        entry.setId(1L);
        entry.setTenantId("t1");
        entry.setEntryType(TdsTcsType.TDS);
        entry.setSectionCode("194C");

        when(tdsTcsService.updateStatus(eq(1L), eq(TdsTcsStatus.DEPOSITED), eq("CERT001")))
                .thenReturn(entry);

        mockMvc.perform(post("/api/tds-tcs/1/status")
                        .param("status", "DEPOSITED")
                        .param("certificateNumber", "CERT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionCode").value("194C"));
    }
}

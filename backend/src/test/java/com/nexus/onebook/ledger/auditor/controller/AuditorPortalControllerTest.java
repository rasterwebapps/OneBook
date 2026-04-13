package com.nexus.onebook.ledger.auditor.controller;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.auditor.model.AuditSampleRequest;
import com.nexus.onebook.ledger.auditor.model.AuditComment;
import com.nexus.onebook.ledger.auditor.model.AuditWorkflow;
import com.nexus.onebook.ledger.auditor.model.AuditWorkflowStatus;
import com.nexus.onebook.ledger.auditor.service.AuditorPortalService;
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

@WebMvcTest(AuditorPortalController.class)
@Import(GlobalExceptionHandler.class)
class AuditorPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditorPortalService auditorPortalService;

    @Test
    void createSampleRequest_returns201() throws Exception {
        AuditSampleRequest entity = new AuditSampleRequest(
                "tenant-1", "John CPA", "john@cpa.com",
                "Q4 samples", "journal_transactions", 10);

        when(auditorPortalService.createSampleRequest(any())).thenReturn(entity);

        mockMvc.perform(post("/api/auditor-portal/sample-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "auditorName": "John CPA",
                                    "auditorEmail": "john@cpa.com",
                                    "requestDescription": "Q4 samples",
                                    "tableName": "journal_transactions",
                                    "sampleSize": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auditorName").value("John CPA"));
    }

    @Test
    void getSampleRequests_returnsList() throws Exception {
        when(auditorPortalService.getSampleRequests("tenant-1"))
                .thenReturn(List.of(new AuditSampleRequest()));

        mockMvc.perform(get("/api/auditor-portal/sample-requests")
                        .param("tenantId", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createComment_returns201() throws Exception {
        AuditComment comment = new AuditComment(
                "tenant-1", "Jane CPA", "journal_transactions", 42L,
                "Please clarify");

        when(auditorPortalService.createComment(any())).thenReturn(comment);

        mockMvc.perform(post("/api/auditor-portal/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "auditorName": "Jane CPA",
                                    "tableName": "journal_transactions",
                                    "recordId": 42,
                                    "commentText": "Please clarify"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auditorName").value("Jane CPA"));
    }

    @Test
    void createWorkflow_returns201() throws Exception {
        AuditWorkflow workflow = new AuditWorkflow("tenant-1", "Year-End Review", "Bob CPA");

        when(auditorPortalService.createWorkflow(any())).thenReturn(workflow);

        mockMvc.perform(post("/api/auditor-portal/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "workflowName": "Year-End Review",
                                    "auditorName": "Bob CPA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workflowName").value("Year-End Review"));
    }

    @Test
    void approveWorkflow_returnsApproved() throws Exception {
        AuditWorkflow workflow = new AuditWorkflow("tenant-1", "Test", "Auditor");
        workflow.setStatus(AuditWorkflowStatus.APPROVED);

        when(auditorPortalService.approveWorkflow(1L)).thenReturn(workflow);

        mockMvc.perform(post("/api/auditor-portal/workflows/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}

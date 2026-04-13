package com.nexus.onebook.ledger.auditor.service;

import com.nexus.onebook.ledger.auditor.dto.AuditCommentRequest;
import com.nexus.onebook.ledger.auditor.dto.AuditSampleRequestDto;
import com.nexus.onebook.ledger.auditor.dto.AuditWorkflowRequest;
import com.nexus.onebook.ledger.auditor.model.AuditComment;
import com.nexus.onebook.ledger.auditor.model.AuditSampleRequest;
import com.nexus.onebook.ledger.auditor.model.AuditWorkflow;
import com.nexus.onebook.ledger.auditor.model.AuditWorkflowStatus;
import com.nexus.onebook.ledger.auditor.repository.AuditCommentRepository;
import com.nexus.onebook.ledger.auditor.repository.AuditSampleRequestRepository;
import com.nexus.onebook.ledger.auditor.repository.AuditWorkflowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditorPortalServiceTest {

    @Mock
    private AuditSampleRequestRepository sampleRequestRepository;
    @Mock
    private AuditCommentRepository commentRepository;
    @Mock
    private AuditWorkflowRepository workflowRepository;

    @InjectMocks
    private AuditorPortalService auditorPortalService;

    @Test
    void createSampleRequest_validRequest_succeeds() {
        AuditSampleRequestDto dto = new AuditSampleRequestDto(
                "tenant-1", "John CPA", "john@cpa.com",
                "Need Q4 transaction samples", "journal_transactions", 25, null, null);

        when(sampleRequestRepository.save(any(AuditSampleRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AuditSampleRequest result = auditorPortalService.createSampleRequest(dto);

        assertNotNull(result);
        assertEquals("tenant-1", result.getTenantId());
        assertEquals("John CPA", result.getAuditorName());
        assertEquals(25, result.getSampleSize());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void getSampleRequests_returnsList() {
        when(sampleRequestRepository.findByTenantId("tenant-1"))
                .thenReturn(List.of(new AuditSampleRequest()));

        List<AuditSampleRequest> result = auditorPortalService.getSampleRequests("tenant-1");

        assertEquals(1, result.size());
    }

    @Test
    void completeSampleRequest_setsStatusCompleted() {
        AuditSampleRequest request = new AuditSampleRequest();
        request.setId(1L);
        request.setStatus("PENDING");

        when(sampleRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(sampleRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuditSampleRequest result = auditorPortalService.completeSampleRequest(1L, "[{\"id\":1}]");

        assertEquals("COMPLETED", result.getStatus());
        assertEquals("[{\"id\":1}]", result.getResponseData());
    }

    @Test
    void createComment_validRequest_succeeds() {
        AuditCommentRequest dto = new AuditCommentRequest(
                "tenant-1", "Jane CPA", "journal_transactions", 42L,
                "Please provide supporting documentation");

        when(commentRepository.save(any(AuditComment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AuditComment result = auditorPortalService.createComment(dto);

        assertNotNull(result);
        assertEquals("tenant-1", result.getTenantId());
        assertEquals("Jane CPA", result.getAuditorName());
        assertEquals(42L, result.getRecordId());
        assertFalse(result.isResolved());
    }

    @Test
    void resolveComment_setsResolvedTrue() {
        AuditComment comment = new AuditComment();
        comment.setId(1L);
        comment.setResolved(false);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuditComment result = auditorPortalService.resolveComment(1L);

        assertTrue(result.isResolved());
    }

    @Test
    void createWorkflow_validRequest_succeeds() {
        AuditWorkflowRequest dto = new AuditWorkflowRequest(
                "tenant-1", "Year-End Close Review", "Annual audit workflow", "Bob CPA");

        when(workflowRepository.save(any(AuditWorkflow.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AuditWorkflow result = auditorPortalService.createWorkflow(dto);

        assertNotNull(result);
        assertEquals("Year-End Close Review", result.getWorkflowName());
        assertEquals(AuditWorkflowStatus.PENDING, result.getStatus());
    }

    @Test
    void approveWorkflow_pendingWorkflow_setsApproved() {
        AuditWorkflow workflow = new AuditWorkflow("tenant-1", "Test", "Auditor");
        workflow.setId(1L);

        when(workflowRepository.findById(1L)).thenReturn(Optional.of(workflow));
        when(workflowRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuditWorkflow result = auditorPortalService.approveWorkflow(1L);

        assertEquals(AuditWorkflowStatus.APPROVED, result.getStatus());
        assertNotNull(result.getApprovedAt());
    }

    @Test
    void rejectWorkflow_pendingWorkflow_setsRejected() {
        AuditWorkflow workflow = new AuditWorkflow("tenant-1", "Test", "Auditor");
        workflow.setId(1L);

        when(workflowRepository.findById(1L)).thenReturn(Optional.of(workflow));
        when(workflowRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuditWorkflow result = auditorPortalService.rejectWorkflow(1L, "Incomplete documentation");

        assertEquals(AuditWorkflowStatus.REJECTED, result.getStatus());
        assertNotNull(result.getRejectedAt());
        assertEquals("Incomplete documentation", result.getRejectionReason());
    }

    @Test
    void approveWorkflow_nonPending_throws() {
        AuditWorkflow workflow = new AuditWorkflow("tenant-1", "Test", "Auditor");
        workflow.setId(1L);
        workflow.setStatus(AuditWorkflowStatus.APPROVED);

        when(workflowRepository.findById(1L)).thenReturn(Optional.of(workflow));

        assertThrows(IllegalArgumentException.class,
                () -> auditorPortalService.approveWorkflow(1L));
    }

    @Test
    void getSampleRequest_notFound_throws() {
        when(sampleRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> auditorPortalService.getSampleRequest(99L));
    }
}

package com.nexus.onebook.auditor.service;

import com.nexus.onebook.auditor.dto.AuditCommentRequest;
import com.nexus.onebook.auditor.dto.AuditSampleRequestDto;
import com.nexus.onebook.auditor.dto.AuditWorkflowRequest;
import com.nexus.onebook.auditor.model.AuditComment;
import com.nexus.onebook.auditor.model.AuditSampleRequest;
import com.nexus.onebook.auditor.model.AuditWorkflow;
import com.nexus.onebook.auditor.model.AuditWorkflowStatus;
import com.nexus.onebook.auditor.repository.AuditCommentRepository;
import com.nexus.onebook.auditor.repository.AuditSampleRequestRepository;
import com.nexus.onebook.auditor.repository.AuditWorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service for the External Auditor Portal.
 * Provides read-only access for CPAs to request samples,
 * leave comments, and manage approval workflows.
 */
@Service
public class AuditorPortalService {

    private final AuditSampleRequestRepository sampleRequestRepository;
    private final AuditCommentRepository commentRepository;
    private final AuditWorkflowRepository workflowRepository;

    public AuditorPortalService(AuditSampleRequestRepository sampleRequestRepository,
                                 AuditCommentRepository commentRepository,
                                 AuditWorkflowRepository workflowRepository) {
        this.sampleRequestRepository = sampleRequestRepository;
        this.commentRepository = commentRepository;
        this.workflowRepository = workflowRepository;
    }

    // --- Sample Requests ---

    @Transactional
    public AuditSampleRequest createSampleRequest(AuditSampleRequestDto request) {
        AuditSampleRequest entity = new AuditSampleRequest(
                request.tenantId(), request.auditorName(), request.auditorEmail(),
                request.requestDescription(), request.tableName(), request.sampleSize());
        entity.setDateFrom(request.dateFrom());
        entity.setDateTo(request.dateTo());
        return sampleRequestRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<AuditSampleRequest> getSampleRequests(String tenantId) {
        return sampleRequestRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public AuditSampleRequest getSampleRequest(Long id) {
        return sampleRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sample request not found: " + id));
    }

    @Transactional
    public AuditSampleRequest completeSampleRequest(Long id, String responseData) {
        AuditSampleRequest request = getSampleRequest(id);
        request.setStatus("COMPLETED");
        request.setResponseData(responseData);
        return sampleRequestRepository.save(request);
    }

    // --- Audit Comments ---

    @Transactional
    public AuditComment createComment(AuditCommentRequest request) {
        AuditComment comment = new AuditComment(
                request.tenantId(), request.auditorName(), request.tableName(),
                request.recordId(), request.commentText());
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<AuditComment> getComments(String tenantId) {
        return commentRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<AuditComment> getCommentsForRecord(String tenantId, String tableName, Long recordId) {
        return commentRepository.findByTenantIdAndTableNameAndRecordId(tenantId, tableName, recordId);
    }

    @Transactional
    public AuditComment resolveComment(Long id) {
        AuditComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Audit comment not found: " + id));
        comment.setResolved(true);
        return commentRepository.save(comment);
    }

    // --- Approval Workflows ---

    @Transactional
    public AuditWorkflow createWorkflow(AuditWorkflowRequest request) {
        AuditWorkflow workflow = new AuditWorkflow(
                request.tenantId(), request.workflowName(), request.auditorName());
        workflow.setDescription(request.description());
        return workflowRepository.save(workflow);
    }

    @Transactional(readOnly = true)
    public List<AuditWorkflow> getWorkflows(String tenantId) {
        return workflowRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public AuditWorkflow getWorkflow(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Audit workflow not found: " + id));
    }

    @Transactional
    public AuditWorkflow approveWorkflow(Long id) {
        AuditWorkflow workflow = getWorkflow(id);
        if (workflow.getStatus() != AuditWorkflowStatus.PENDING) {
            throw new IllegalArgumentException("Workflow is not in PENDING status");
        }
        workflow.setStatus(AuditWorkflowStatus.APPROVED);
        workflow.setApprovedAt(Instant.now());
        return workflowRepository.save(workflow);
    }

    @Transactional
    public AuditWorkflow rejectWorkflow(Long id, String reason) {
        AuditWorkflow workflow = getWorkflow(id);
        if (workflow.getStatus() != AuditWorkflowStatus.PENDING) {
            throw new IllegalArgumentException("Workflow is not in PENDING status");
        }
        workflow.setStatus(AuditWorkflowStatus.REJECTED);
        workflow.setRejectedAt(Instant.now());
        workflow.setRejectionReason(reason);
        return workflowRepository.save(workflow);
    }
}

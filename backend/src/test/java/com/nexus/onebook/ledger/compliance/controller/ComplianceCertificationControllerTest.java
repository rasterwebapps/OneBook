package com.nexus.onebook.ledger.compliance.controller;

import com.nexus.onebook.ledger.exception.GlobalExceptionHandler;
import com.nexus.onebook.ledger.compliance.model.CertificationStatus;
import com.nexus.onebook.ledger.compliance.model.ComplianceCertification;
import com.nexus.onebook.ledger.compliance.service.ComplianceCertificationService;
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

@WebMvcTest(ComplianceCertificationController.class)
@Import(GlobalExceptionHandler.class)
class ComplianceCertificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComplianceCertificationService certificationService;

    @Test
    void createCertification_returns201() throws Exception {
        ComplianceCertification cert = new ComplianceCertification(
                "tenant-1", "SOC 2 Type II", "AICPA", "Technology");

        when(certificationService.createCertification(any())).thenReturn(cert);

        mockMvc.perform(post("/api/compliance-certifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tenantId": "tenant-1",
                                    "certificationName": "SOC 2 Type II",
                                    "issuingBody": "AICPA",
                                    "industry": "Technology"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.certificationName").value("SOC 2 Type II"));
    }

    @Test
    void getCertifications_returnsList() throws Exception {
        when(certificationService.getCertifications("tenant-1"))
                .thenReturn(List.of(new ComplianceCertification()));

        mockMvc.perform(get("/api/compliance-certifications")
                        .param("tenantId", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void startCertification_returnsInProgress() throws Exception {
        ComplianceCertification cert = new ComplianceCertification(
                "tenant-1", "ISO 27001", "ISO", "Technology");
        cert.setStatus(CertificationStatus.IN_PROGRESS);

        when(certificationService.startCertification(1L)).thenReturn(cert);

        mockMvc.perform(post("/api/compliance-certifications/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}

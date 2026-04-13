package com.nexus.onebook.auditor.controller;

import com.nexus.onebook.auditor.dto.SecurityAuditReport;
import com.nexus.onebook.exception.GlobalExceptionHandler;
import com.nexus.onebook.auditor.service.SecurityAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SecurityAuditController.class)
@Import(GlobalExceptionHandler.class)
class SecurityAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SecurityAuditService securityAuditService;

    @Test
    void runAudit_returnsReport() throws Exception {
        SecurityAuditReport report = new SecurityAuditReport(
                "tenant-1", Instant.now(),
                true, true, true, 5, 5, 0,
                List.of("PASS: All checks passed"));

        when(securityAuditService.runSecurityAudit("tenant-1")).thenReturn(report);

        mockMvc.perform(get("/api/security-audit/run")
                        .param("tenantId", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.encryptionVerified").value(true))
                .andExpect(jsonPath("$.keyManagementVerified").value(true))
                .andExpect(jsonPath("$.auditChainIntact").value(true))
                .andExpect(jsonPath("$.totalChecks").value(5));
    }
}

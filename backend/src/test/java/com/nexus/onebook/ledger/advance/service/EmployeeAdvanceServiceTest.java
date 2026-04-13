package com.nexus.onebook.ledger.advance.service;

import com.nexus.onebook.ledger.advance.dto.AdvanceResponse;
import com.nexus.onebook.ledger.advance.dto.CreateAdvanceRequest;
import com.nexus.onebook.ledger.advance.model.*;
import com.nexus.onebook.ledger.advance.repository.*;
import com.nexus.onebook.ledger.security.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeAdvanceServiceTest {

    @Mock private EmployeeAdvanceRepository advanceRepository;
    @Mock private EmployeeAdvanceConfigRepository configRepository;
    @Mock private EmployeeAdvanceBalanceRepository balanceRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private EmployeeAdvanceService advanceService;

    private static final String TENANT = "tenant-1";
    private static final Long EMPLOYEE_ID = 100L;
    private static final Long DEPARTMENT_ID = 10L;

    @Test
    void createAdvance_withinLimit_createsSuccessfully() {
        CreateAdvanceRequest request = new CreateAdvanceRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("5000.00"), "Business travel", LocalDate.now(),
                "user1", false, null
        );

        when(configRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.empty());
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.empty());
        when(advanceRepository.save(any(EmployeeAdvance.class))).thenAnswer(inv -> {
            EmployeeAdvance a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AdvanceResponse response = advanceService.createAdvance(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.amount());
        assertEquals("PENDING_HOD_APPROVAL", response.status());
        assertEquals("HOD", response.currentApproverRole());
        verify(advanceRepository).save(any(EmployeeAdvance.class));
    }

    @Test
    void createAdvance_exceedsLimit_throwsException() {
        CreateAdvanceRequest request = new CreateAdvanceRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("15000.00"), "Large expense", LocalDate.now(),
                "user1", false, null
        );

        // Employee already has 2000 outstanding
        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("2000.00"));
        when(configRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.empty());
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(balance));

        assertThrows(IllegalStateException.class, () -> advanceService.createAdvance(request));
    }

    @Test
    void createAdvance_withOverride_createsSuccessfully() {
        CreateAdvanceRequest request = new CreateAdvanceRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("15000.00"), "Urgent expense", LocalDate.now(),
                "user1", true, "CEO approved exception"
        );

        when(configRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.empty());
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.empty());
        when(advanceRepository.save(any(EmployeeAdvance.class))).thenAnswer(inv -> {
            EmployeeAdvance a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AdvanceResponse response = advanceService.createAdvance(request);

        assertNotNull(response);
        assertTrue(response.overrideFlag());
        assertEquals("CEO approved exception", response.overrideReason());
    }

    @Test
    void createAdvance_overrideWithoutReason_throwsException() {
        CreateAdvanceRequest request = new CreateAdvanceRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("15000.00"), "Expense", LocalDate.now(),
                "user1", true, null
        );

        when(configRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.empty());
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> advanceService.createAdvance(request));
    }

    @Test
    void createAdvance_negativeAmount_throwsException() {
        CreateAdvanceRequest request = new CreateAdvanceRequest(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                new BigDecimal("-100.00"), "Invalid", LocalDate.now(),
                "user1", false, null
        );

        assertThrows(IllegalArgumentException.class, () -> advanceService.createAdvance(request));
    }

    @Test
    void approveAdvance_hodApproval_smallAmount_finalizesApproval() {
        EmployeeAdvance advance = buildAdvance(1L, new BigDecimal("5000.00"), AdvanceStatus.PENDING_HOD_APPROVAL);

        when(advanceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advance));
        when(advanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID)));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdvanceResponse response = advanceService.approveAdvance(TENANT, 1L, "hod_user", ApproverRole.HOD, null);

        assertEquals("APPROVED", response.status());
        assertEquals("hod_user", response.hodApprovedBy());
        assertNotNull(response.hodApprovedAt());
        assertNull(response.currentApproverRole());
    }

    @Test
    void approveAdvance_hodApproval_mediumAmount_escalatesToCeo() {
        EmployeeAdvance advance = buildAdvance(1L, new BigDecimal("15000.00"), AdvanceStatus.PENDING_HOD_APPROVAL);

        when(advanceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advance));
        when(advanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdvanceResponse response = advanceService.approveAdvance(TENANT, 1L, "hod_user", ApproverRole.HOD, null);

        assertEquals("PENDING_CEO_APPROVAL", response.status());
        assertEquals("CEO", response.currentApproverRole());
    }

    @Test
    void approveAdvance_ceoApproval_largeAmount_escalatesToMd() {
        EmployeeAdvance advance = buildAdvance(1L, new BigDecimal("25000.00"), AdvanceStatus.PENDING_CEO_APPROVAL);
        advance.setHodApprovedBy("hod_user");

        when(advanceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advance));
        when(advanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdvanceResponse response = advanceService.approveAdvance(TENANT, 1L, "ceo_user", ApproverRole.CEO, null);

        assertEquals("PENDING_MD_APPROVAL", response.status());
        assertEquals("MD", response.currentApproverRole());
    }

    @Test
    void approveAdvance_mdApproval_finalizesApproval() {
        EmployeeAdvance advance = buildAdvance(1L, new BigDecimal("25000.00"), AdvanceStatus.PENDING_MD_APPROVAL);
        advance.setHodApprovedBy("hod_user");
        advance.setCeoApprovedBy("ceo_user");

        when(advanceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advance));
        when(advanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID)));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdvanceResponse response = advanceService.approveAdvance(TENANT, 1L, "md_user", ApproverRole.MD, null);

        assertEquals("APPROVED", response.status());
        assertEquals("md_user", response.mdApprovedBy());
    }

    @Test
    void approveAdvance_selfApproval_throwsException() {
        EmployeeAdvance advance = buildAdvance(1L, new BigDecimal("5000.00"), AdvanceStatus.PENDING_HOD_APPROVAL);
        advance.setCreatedBy("same_user");

        when(advanceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advance));

        assertThrows(IllegalStateException.class, () ->
                advanceService.approveAdvance(TENANT, 1L, "same_user", ApproverRole.HOD, null));
    }

    @Test
    void approveAdvance_wrongRole_throwsException() {
        EmployeeAdvance advance = buildAdvance(1L, new BigDecimal("5000.00"), AdvanceStatus.PENDING_HOD_APPROVAL);

        when(advanceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advance));

        assertThrows(IllegalStateException.class, () ->
                advanceService.approveAdvance(TENANT, 1L, "ceo_user", ApproverRole.CEO, null));
    }

    @Test
    void rejectAdvance_setsRejectedStatus() {
        EmployeeAdvance advance = buildAdvance(1L, new BigDecimal("5000.00"), AdvanceStatus.PENDING_HOD_APPROVAL);

        when(advanceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advance));
        when(advanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdvanceResponse response = advanceService.rejectAdvance(TENANT, 1L, "hod_user", ApproverRole.HOD, "Insufficient documentation");

        assertEquals("REJECTED", response.status());
        assertEquals("hod_user", response.rejectedBy());
        assertEquals("Insufficient documentation", response.rejectionReason());
    }

    @Test
    void rejectAdvance_withoutReason_throwsException() {
        EmployeeAdvance advance = buildAdvance(1L, new BigDecimal("5000.00"), AdvanceStatus.PENDING_HOD_APPROVAL);

        when(advanceRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(advance));

        assertThrows(IllegalArgumentException.class, () ->
                advanceService.rejectAdvance(TENANT, 1L, "hod_user", ApproverRole.HOD, null));
    }

    @Test
    void getOutstandingBalance_returnsBalance() {
        EmployeeAdvanceBalance balance = new EmployeeAdvanceBalance(TENANT, EMPLOYEE_ID);
        balance.setOutstandingAdvance(new BigDecimal("7500.00"));

        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.of(balance));

        BigDecimal result = advanceService.getOutstandingBalance(TENANT, EMPLOYEE_ID);

        assertEquals(new BigDecimal("7500.00"), result);
    }

    @Test
    void getOutstandingBalance_noBalance_returnsZero() {
        when(balanceRepository.findByTenantIdAndEmployeeId(TENANT, EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        BigDecimal result = advanceService.getOutstandingBalance(TENANT, EMPLOYEE_ID);

        assertEquals(BigDecimal.ZERO, result);
    }

    private EmployeeAdvance buildAdvance(Long id, BigDecimal amount, AdvanceStatus status) {
        EmployeeAdvance advance = new EmployeeAdvance(
                TENANT, EMPLOYEE_ID, DEPARTMENT_ID,
                amount, "Test purpose", LocalDate.now(), "user1"
        );
        advance.setId(id);
        advance.setStatus(status);
        if (status == AdvanceStatus.PENDING_HOD_APPROVAL) {
            advance.setCurrentApproverRole(ApproverRole.HOD);
        } else if (status == AdvanceStatus.PENDING_CEO_APPROVAL) {
            advance.setCurrentApproverRole(ApproverRole.CEO);
        } else if (status == AdvanceStatus.PENDING_MD_APPROVAL) {
            advance.setCurrentApproverRole(ApproverRole.MD);
        }
        return advance;
    }
}

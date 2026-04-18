package com.nexus.onebook.education;

import com.nexus.onebook.education.dto.CreateFinalizationRequest;
import com.nexus.onebook.education.dto.FeeFinalizationDto;
import com.nexus.onebook.education.model.*;
import com.nexus.onebook.education.repository.FeeFinalizationRepository;
import com.nexus.onebook.education.repository.StudentEnquiryRepository;
import com.nexus.onebook.education.service.FeeFinalizationService;
import com.nexus.onebook.education.service.FeeStructureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FeeFinalizationService#finalizeEnquiry.
 */
@ExtendWith(MockitoExtension.class)
class FeeFinalizationServiceTest {

    @Mock private FeeFinalizationRepository finalizationRepository;
    @Mock private StudentEnquiryRepository enquiryRepository;
    @Mock private FeeStructureService feeStructureService;

    @InjectMocks
    private FeeFinalizationService feeFinalizationService;

    private static final String TENANT = "tenant-edu-1";
    private static final UUID ENQUIRY_ID = UUID.randomUUID();

    // -------------------------------------------------------------------------
    // finalizeEnquiry tests
    // -------------------------------------------------------------------------

    @Test
    void finalizeEnquiry_validDiscount_computesFinalPayableCorrectly() {
        StudentEnquiry enquiry = buildOpenEnquiry(
                new BigDecimal("60000.00"), new BigDecimal("8000.00"));
        when(enquiryRepository.findByIdAndTenantId(ENQUIRY_ID, TENANT))
                .thenReturn(Optional.of(enquiry));
        when(finalizationRepository.save(any(FeeFinalization.class))).thenAnswer(inv -> {
            FeeFinalization f = inv.getArgument(0);
            f.setId(UUID.randomUUID());
            return f;
        });
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateFinalizationRequest request = new CreateFinalizationRequest(
                ENQUIRY_ID, new BigDecimal("5000.00"), "admin"
        );

        FeeFinalizationDto result = feeFinalizationService.finalizeEnquiry(request, TENANT);

        // finalPayable = 60000 + 8000 - 5000 = 63000
        assertEquals(new BigDecimal("63000.00"), result.finalPayable());
        assertEquals(new BigDecimal("5000.00"), result.discountAmount());
        assertEquals(new BigDecimal("60000.00"), result.genericTotal());
        assertEquals(new BigDecimal("8000.00"), result.additionalFee());
        assertEquals("admin", result.finalizedBy());
    }

    @Test
    void finalizeEnquiry_zeroDiscount_finalPayableEqualsTotalFees() {
        StudentEnquiry enquiry = buildOpenEnquiry(
                new BigDecimal("50000.00"), new BigDecimal("30000.00"));
        when(enquiryRepository.findByIdAndTenantId(ENQUIRY_ID, TENANT))
                .thenReturn(Optional.of(enquiry));
        when(finalizationRepository.save(any(FeeFinalization.class))).thenAnswer(inv -> {
            FeeFinalization f = inv.getArgument(0);
            f.setId(UUID.randomUUID());
            return f;
        });
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateFinalizationRequest request = new CreateFinalizationRequest(
                ENQUIRY_ID, BigDecimal.ZERO, "admin"
        );

        FeeFinalizationDto result = feeFinalizationService.finalizeEnquiry(request, TENANT);

        assertEquals(new BigDecimal("80000.00"), result.finalPayable());
    }

    @Test
    void finalizeEnquiry_discountEqualsTotalFees_finalPayableIsZero() {
        BigDecimal generic = new BigDecimal("40000.00");
        BigDecimal additional = new BigDecimal("10000.00");
        StudentEnquiry enquiry = buildOpenEnquiry(generic, additional);
        when(enquiryRepository.findByIdAndTenantId(ENQUIRY_ID, TENANT))
                .thenReturn(Optional.of(enquiry));
        when(finalizationRepository.save(any(FeeFinalization.class))).thenAnswer(inv -> {
            FeeFinalization f = inv.getArgument(0);
            f.setId(UUID.randomUUID());
            return f;
        });
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateFinalizationRequest request = new CreateFinalizationRequest(
                ENQUIRY_ID, new BigDecimal("50000.00"), "admin"
        );

        FeeFinalizationDto result = feeFinalizationService.finalizeEnquiry(request, TENANT);

        // Use compareTo because BigDecimal.ZERO (scale 0) != 0.00 (scale 2) via equals()
        assertEquals(0, result.finalPayable().compareTo(BigDecimal.ZERO),
                "finalPayable should be zero when discount equals total fees");
    }

    @Test
    void finalizeEnquiry_discountExceedsTotalFees_throwsIllegalArgumentException() {
        StudentEnquiry enquiry = buildOpenEnquiry(
                new BigDecimal("30000.00"), new BigDecimal("5000.00"));
        when(enquiryRepository.findByIdAndTenantId(ENQUIRY_ID, TENANT))
                .thenReturn(Optional.of(enquiry));

        CreateFinalizationRequest request = new CreateFinalizationRequest(
                ENQUIRY_ID, new BigDecimal("40000.00"), "admin"
        );

        assertThrows(IllegalArgumentException.class,
                () -> feeFinalizationService.finalizeEnquiry(request, TENANT));
    }

    @Test
    void finalizeEnquiry_alreadyFinalizedEnquiry_throwsIllegalStateException() {
        StudentEnquiry enquiry = buildEnquiry(
                new BigDecimal("50000.00"), new BigDecimal("8000.00"), EnquiryStatus.FINALIZED);
        when(enquiryRepository.findByIdAndTenantId(ENQUIRY_ID, TENANT))
                .thenReturn(Optional.of(enquiry));

        CreateFinalizationRequest request = new CreateFinalizationRequest(
                ENQUIRY_ID, new BigDecimal("1000.00"), "admin"
        );

        assertThrows(IllegalStateException.class,
                () -> feeFinalizationService.finalizeEnquiry(request, TENANT));
    }

    @Test
    void finalizeEnquiry_cancelledEnquiry_throwsIllegalStateException() {
        StudentEnquiry enquiry = buildEnquiry(
                new BigDecimal("50000.00"), new BigDecimal("8000.00"), EnquiryStatus.CANCELLED);
        when(enquiryRepository.findByIdAndTenantId(ENQUIRY_ID, TENANT))
                .thenReturn(Optional.of(enquiry));

        CreateFinalizationRequest request = new CreateFinalizationRequest(
                ENQUIRY_ID, new BigDecimal("1000.00"), "admin"
        );

        assertThrows(IllegalStateException.class,
                () -> feeFinalizationService.finalizeEnquiry(request, TENANT));
    }

    @Test
    void finalizeEnquiry_enquiryNotFound_throwsIllegalArgumentException() {
        when(enquiryRepository.findByIdAndTenantId(ENQUIRY_ID, TENANT))
                .thenReturn(Optional.empty());

        CreateFinalizationRequest request = new CreateFinalizationRequest(
                ENQUIRY_ID, new BigDecimal("1000.00"), "admin"
        );

        assertThrows(IllegalArgumentException.class,
                () -> feeFinalizationService.finalizeEnquiry(request, TENANT));
    }

    @Test
    void finalizeEnquiry_updatesEnquiryStatusToFinalized() {
        StudentEnquiry enquiry = buildOpenEnquiry(
                new BigDecimal("50000.00"), new BigDecimal("10000.00"));
        when(enquiryRepository.findByIdAndTenantId(ENQUIRY_ID, TENANT))
                .thenReturn(Optional.of(enquiry));
        when(finalizationRepository.save(any(FeeFinalization.class))).thenAnswer(inv -> {
            FeeFinalization f = inv.getArgument(0);
            f.setId(UUID.randomUUID());
            return f;
        });
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateFinalizationRequest request = new CreateFinalizationRequest(
                ENQUIRY_ID, new BigDecimal("2000.00"), "admin"
        );

        feeFinalizationService.finalizeEnquiry(request, TENANT);

        assertEquals(EnquiryStatus.FINALIZED, enquiry.getStatus());
        verify(enquiryRepository).save(enquiry);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private StudentEnquiry buildOpenEnquiry(BigDecimal genericTotal, BigDecimal additionalFee) {
        return buildEnquiry(genericTotal, additionalFee, EnquiryStatus.OPEN);
    }

    private StudentEnquiry buildEnquiry(BigDecimal genericTotal, BigDecimal additionalFee, EnquiryStatus status) {
        EducationProgram program = new EducationProgram(TENANT, "B.Tech", "BTECH");
        program.setId(UUID.randomUUID());

        EducationCourse course = new EducationCourse(TENANT, program, "Computer Science", "CS");
        course.setId(UUID.randomUUID());

        StudentEnquiry enquiry = new StudentEnquiry(
                TENANT, "Test Student", "test@example.com", "9000000000",
                program, course, StudentType.DAY_SCHOLAR,
                genericTotal, additionalFee, genericTotal.add(additionalFee), "2024-25"
        );
        enquiry.setId(ENQUIRY_ID);
        enquiry.setStatus(status);
        return enquiry;
    }
}

package com.nexus.onebook.education;

import com.nexus.onebook.education.dto.EnquiryFeeBreakdownDto;
import com.nexus.onebook.education.model.*;
import com.nexus.onebook.education.repository.*;
import com.nexus.onebook.education.service.FeeStructureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FeeStructureService#getFeeBreakdown.
 */
@ExtendWith(MockitoExtension.class)
class FeeStructureServiceTest {

    @Mock private FeeStructureRepository feeStructureRepository;
    @Mock private FeeStructureItemRepository feeStructureItemRepository;
    @Mock private EducationCourseRepository courseRepository;
    @Mock private FeeTypeRepository feeTypeRepository;

    @InjectMocks
    private FeeStructureService feeStructureService;

    private static final String TENANT = "tenant-edu-1";
    private static final UUID COURSE_ID = UUID.randomUUID();

    // -------------------------------------------------------------------------
    // getFeeBreakdown tests
    // -------------------------------------------------------------------------

    @Test
    void getFeeBreakdown_onlyGenericItems_returnsCorrectGenericTotal() {
        FeeStructure structure = buildFeeStructure(COURSE_ID);
        when(feeStructureRepository.findByCourseIdAndTenantIdAndIsActiveTrue(COURSE_ID, TENANT))
                .thenReturn(Optional.of(structure));

        FeeType tuition = buildFeeType(FeeCategory.GENERIC, null);
        FeeType library = buildFeeType(FeeCategory.GENERIC, null);
        FeeStructureItem item1 = buildItem(structure, tuition, new BigDecimal("50000.00"));
        FeeStructureItem item2 = buildItem(structure, library, new BigDecimal("5000.00"));

        when(feeStructureItemRepository.findAllByFeeStructureIdAndTenantId(structure.getId(), TENANT))
                .thenReturn(List.of(item1, item2));

        EnquiryFeeBreakdownDto breakdown = feeStructureService.getFeeBreakdown(COURSE_ID, TENANT);

        assertEquals(new BigDecimal("55000.00"), breakdown.genericTotal());
        assertEquals(BigDecimal.ZERO, breakdown.hostelFee());
        assertEquals(BigDecimal.ZERO, breakdown.transportationFee());
    }

    @Test
    void getFeeBreakdown_missingAdditionalTypes_returnsZeroForBothAdditional() {
        FeeStructure structure = buildFeeStructure(COURSE_ID);
        when(feeStructureRepository.findByCourseIdAndTenantIdAndIsActiveTrue(COURSE_ID, TENANT))
                .thenReturn(Optional.of(structure));

        FeeType tuition = buildFeeType(FeeCategory.GENERIC, null);
        FeeStructureItem item = buildItem(structure, tuition, new BigDecimal("40000.00"));

        when(feeStructureItemRepository.findAllByFeeStructureIdAndTenantId(structure.getId(), TENANT))
                .thenReturn(List.of(item));

        EnquiryFeeBreakdownDto breakdown = feeStructureService.getFeeBreakdown(COURSE_ID, TENANT);

        assertEquals(new BigDecimal("40000.00"), breakdown.genericTotal());
        assertEquals(BigDecimal.ZERO, breakdown.hostelFee());
        assertEquals(BigDecimal.ZERO, breakdown.transportationFee());
    }

    @Test
    void getFeeBreakdown_mixedItems_returnsAllThreeCorrectly() {
        FeeStructure structure = buildFeeStructure(COURSE_ID);
        when(feeStructureRepository.findByCourseIdAndTenantIdAndIsActiveTrue(COURSE_ID, TENANT))
                .thenReturn(Optional.of(structure));

        FeeType tuition = buildFeeType(FeeCategory.GENERIC, null);
        FeeType sports = buildFeeType(FeeCategory.GENERIC, null);
        FeeType hostelType = buildFeeType(FeeCategory.ADDITIONAL, AdditionalFeeType.HOSTEL);
        FeeType transportType = buildFeeType(FeeCategory.ADDITIONAL, AdditionalFeeType.TRANSPORTATION);

        FeeStructureItem tuitionItem = buildItem(structure, tuition, new BigDecimal("60000.00"));
        FeeStructureItem sportsItem = buildItem(structure, sports, new BigDecimal("2000.00"));
        FeeStructureItem hostelItem = buildItem(structure, hostelType, new BigDecimal("30000.00"));
        FeeStructureItem transportItem = buildItem(structure, transportType, new BigDecimal("8000.00"));

        when(feeStructureItemRepository.findAllByFeeStructureIdAndTenantId(structure.getId(), TENANT))
                .thenReturn(List.of(tuitionItem, sportsItem, hostelItem, transportItem));

        EnquiryFeeBreakdownDto breakdown = feeStructureService.getFeeBreakdown(COURSE_ID, TENANT);

        assertEquals(new BigDecimal("62000.00"), breakdown.genericTotal());
        assertEquals(new BigDecimal("30000.00"), breakdown.hostelFee());
        assertEquals(new BigDecimal("8000.00"), breakdown.transportationFee());
    }

    @Test
    void getFeeBreakdown_noFeeStructure_throwsIllegalArgumentException() {
        when(feeStructureRepository.findByCourseIdAndTenantIdAndIsActiveTrue(COURSE_ID, TENANT))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> feeStructureService.getFeeBreakdown(COURSE_ID, TENANT));
    }

    @Test
    void getFeeBreakdown_emptyItems_returnsAllZeros() {
        FeeStructure structure = buildFeeStructure(COURSE_ID);
        when(feeStructureRepository.findByCourseIdAndTenantIdAndIsActiveTrue(COURSE_ID, TENANT))
                .thenReturn(Optional.of(structure));
        when(feeStructureItemRepository.findAllByFeeStructureIdAndTenantId(structure.getId(), TENANT))
                .thenReturn(List.of());

        EnquiryFeeBreakdownDto breakdown = feeStructureService.getFeeBreakdown(COURSE_ID, TENANT);

        assertEquals(BigDecimal.ZERO, breakdown.genericTotal());
        assertEquals(BigDecimal.ZERO, breakdown.hostelFee());
        assertEquals(BigDecimal.ZERO, breakdown.transportationFee());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private FeeStructure buildFeeStructure(UUID courseId) {
        EducationProgram program = new EducationProgram(TENANT, "B.Tech", "BTECH");
        program.setId(UUID.randomUUID());

        EducationCourse course = new EducationCourse(TENANT, program, "Computer Science", "CS");
        course.setId(courseId);

        FeeStructure structure = new FeeStructure(TENANT, course, "2024-25");
        structure.setId(UUID.randomUUID());
        return structure;
    }

    private FeeType buildFeeType(FeeCategory category, AdditionalFeeType additionalType) {
        FeeType ft = new FeeType(TENANT, "Some Fee", category, additionalType);
        ft.setId(UUID.randomUUID());
        return ft;
    }

    private FeeStructureItem buildItem(FeeStructure structure, FeeType feeType, BigDecimal amount) {
        FeeStructureItem item = new FeeStructureItem(TENANT, structure, feeType, amount);
        item.setId(UUID.randomUUID());
        return item;
    }
}

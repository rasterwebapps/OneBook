package com.nexus.onebook.education;

import com.nexus.onebook.education.dto.CreateEnquiryRequest;
import com.nexus.onebook.education.dto.EnquiryFeeBreakdownDto;
import com.nexus.onebook.education.dto.StudentEnquiryDto;
import com.nexus.onebook.education.model.*;
import com.nexus.onebook.education.repository.*;
import com.nexus.onebook.education.service.FeeStructureService;
import com.nexus.onebook.education.service.StudentEnquiryService;
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
 * Unit tests for StudentEnquiryService#createEnquiry.
 * Verifies fee assignment logic for DAY_SCHOLAR and HOSTELER student types.
 */
@ExtendWith(MockitoExtension.class)
class StudentEnquiryServiceTest {

    @Mock private StudentEnquiryRepository enquiryRepository;
    @Mock private EducationProgramRepository programRepository;
    @Mock private EducationCourseRepository courseRepository;
    @Mock private FeeStructureService feeStructureService;

    @InjectMocks
    private StudentEnquiryService studentEnquiryService;

    private static final String TENANT = "tenant-edu-1";
    private static final UUID PROGRAM_ID = UUID.randomUUID();
    private static final UUID COURSE_ID = UUID.randomUUID();

    // -------------------------------------------------------------------------
    // createEnquiry tests
    // -------------------------------------------------------------------------

    @Test
    void createEnquiry_dayScholar_addsTransportationFeeAsAdditional() {
        setupProgramAndCourse();
        EnquiryFeeBreakdownDto breakdown = new EnquiryFeeBreakdownDto(
                new BigDecimal("60000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("8000.00")
        );
        when(feeStructureService.getFeeBreakdown(COURSE_ID, TENANT)).thenReturn(breakdown);
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> {
            StudentEnquiry e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        CreateEnquiryRequest request = new CreateEnquiryRequest(
                "Alice", "alice@example.com", "9876543210",
                PROGRAM_ID, COURSE_ID, StudentType.DAY_SCHOLAR, "2024-25"
        );

        StudentEnquiryDto result = studentEnquiryService.createEnquiry(request, TENANT);

        assertEquals(new BigDecimal("60000.00"), result.genericTotal());
        // Day scholar gets transportation fee
        assertEquals(new BigDecimal("8000.00"), result.additionalFee());
        assertEquals(new BigDecimal("68000.00"), result.totalFees());
        assertEquals(StudentType.DAY_SCHOLAR, result.studentType());
    }

    @Test
    void createEnquiry_hosteler_addsHostelFeeAsAdditional() {
        setupProgramAndCourse();
        EnquiryFeeBreakdownDto breakdown = new EnquiryFeeBreakdownDto(
                new BigDecimal("60000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("8000.00")
        );
        when(feeStructureService.getFeeBreakdown(COURSE_ID, TENANT)).thenReturn(breakdown);
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> {
            StudentEnquiry e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        CreateEnquiryRequest request = new CreateEnquiryRequest(
                "Bob", "bob@example.com", "9123456789",
                PROGRAM_ID, COURSE_ID, StudentType.HOSTELER, "2024-25"
        );

        StudentEnquiryDto result = studentEnquiryService.createEnquiry(request, TENANT);

        assertEquals(new BigDecimal("60000.00"), result.genericTotal());
        // Hosteler gets hostel fee
        assertEquals(new BigDecimal("30000.00"), result.additionalFee());
        assertEquals(new BigDecimal("90000.00"), result.totalFees());
        assertEquals(StudentType.HOSTELER, result.studentType());
    }

    @Test
    void createEnquiry_dayScholar_totalFeesIsCorrectSum() {
        setupProgramAndCourse();
        BigDecimal generic = new BigDecimal("45000.00");
        BigDecimal transport = new BigDecimal("6000.00");
        EnquiryFeeBreakdownDto breakdown = new EnquiryFeeBreakdownDto(
                generic, new BigDecimal("25000.00"), transport
        );
        when(feeStructureService.getFeeBreakdown(COURSE_ID, TENANT)).thenReturn(breakdown);
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> {
            StudentEnquiry e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        CreateEnquiryRequest request = new CreateEnquiryRequest(
                "Carol", "carol@example.com", "9000000001",
                PROGRAM_ID, COURSE_ID, StudentType.DAY_SCHOLAR, "2024-25"
        );

        StudentEnquiryDto result = studentEnquiryService.createEnquiry(request, TENANT);

        BigDecimal expectedTotal = generic.add(transport);
        assertEquals(expectedTotal, result.totalFees());
    }

    @Test
    void createEnquiry_hosteler_totalFeesIsCorrectSum() {
        setupProgramAndCourse();
        BigDecimal generic = new BigDecimal("45000.00");
        BigDecimal hostel = new BigDecimal("25000.00");
        EnquiryFeeBreakdownDto breakdown = new EnquiryFeeBreakdownDto(
                generic, hostel, new BigDecimal("6000.00")
        );
        when(feeStructureService.getFeeBreakdown(COURSE_ID, TENANT)).thenReturn(breakdown);
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> {
            StudentEnquiry e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        CreateEnquiryRequest request = new CreateEnquiryRequest(
                "Dave", "dave@example.com", "9000000002",
                PROGRAM_ID, COURSE_ID, StudentType.HOSTELER, "2024-25"
        );

        StudentEnquiryDto result = studentEnquiryService.createEnquiry(request, TENANT);

        BigDecimal expectedTotal = generic.add(hostel);
        assertEquals(expectedTotal, result.totalFees());
    }

    @Test
    void createEnquiry_programNotFound_throwsIllegalArgumentException() {
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.empty());

        CreateEnquiryRequest request = new CreateEnquiryRequest(
                "Eve", "eve@example.com", "9000000003",
                PROGRAM_ID, COURSE_ID, StudentType.DAY_SCHOLAR, "2024-25"
        );

        assertThrows(IllegalArgumentException.class,
                () -> studentEnquiryService.createEnquiry(request, TENANT));
    }

    @Test
    void createEnquiry_courseNotFound_throwsIllegalArgumentException() {
        EducationProgram program = buildProgram();
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        CreateEnquiryRequest request = new CreateEnquiryRequest(
                "Frank", "frank@example.com", "9000000004",
                PROGRAM_ID, COURSE_ID, StudentType.HOSTELER, "2024-25"
        );

        assertThrows(IllegalArgumentException.class,
                () -> studentEnquiryService.createEnquiry(request, TENANT));
    }

    @Test
    void createEnquiry_setsStatusToOpen() {
        setupProgramAndCourse();
        EnquiryFeeBreakdownDto breakdown = new EnquiryFeeBreakdownDto(
                new BigDecimal("50000.00"), new BigDecimal("20000.00"), new BigDecimal("5000.00")
        );
        when(feeStructureService.getFeeBreakdown(COURSE_ID, TENANT)).thenReturn(breakdown);
        when(enquiryRepository.save(any(StudentEnquiry.class))).thenAnswer(inv -> {
            StudentEnquiry e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        CreateEnquiryRequest request = new CreateEnquiryRequest(
                "Grace", "grace@example.com", "9000000005",
                PROGRAM_ID, COURSE_ID, StudentType.HOSTELER, "2024-25"
        );

        StudentEnquiryDto result = studentEnquiryService.createEnquiry(request, TENANT);

        assertEquals(EnquiryStatus.OPEN, result.status());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private EducationProgram buildProgram() {
        EducationProgram program = new EducationProgram(TENANT, "B.Tech", "BTECH");
        program.setId(PROGRAM_ID);
        return program;
    }

    private void setupProgramAndCourse() {
        EducationProgram program = buildProgram();
        EducationCourse course = new EducationCourse(TENANT, program, "Computer Science", "CS");
        course.setId(COURSE_ID);

        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
    }
}

package com.nexus.onebook.education.service;

import com.nexus.onebook.education.dto.CreateEnquiryRequest;
import com.nexus.onebook.education.dto.EnquiryFeeBreakdownDto;
import com.nexus.onebook.education.dto.StudentEnquiryDto;
import com.nexus.onebook.education.model.*;
import com.nexus.onebook.education.repository.EducationCourseRepository;
import com.nexus.onebook.education.repository.EducationProgramRepository;
import com.nexus.onebook.education.repository.StudentEnquiryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing student fee enquiries.
 * Automatically computes fees from the active fee structure based on student type.
 */
@Service
public class StudentEnquiryService {

    private final StudentEnquiryRepository enquiryRepository;
    private final EducationProgramRepository programRepository;
    private final EducationCourseRepository courseRepository;
    private final FeeStructureService feeStructureService;

    public StudentEnquiryService(
            StudentEnquiryRepository enquiryRepository,
            EducationProgramRepository programRepository,
            EducationCourseRepository courseRepository,
            FeeStructureService feeStructureService) {
        this.enquiryRepository = enquiryRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.feeStructureService = feeStructureService;
    }

    /**
     * Creates a new student fee enquiry.
     * Fee breakdown is derived from the active fee structure for the course.
     * - DAY_SCHOLAR: additionalFee = transportationFee
     * - HOSTELER:    additionalFee = hostelFee
     * - totalFees = genericTotal + additionalFee
     */
    @Transactional
    public StudentEnquiryDto createEnquiry(CreateEnquiryRequest request, String tenantId) {
        EducationProgram program = programRepository.findById(request.programId())
                .filter(p -> p.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Program not found: " + request.programId()));

        EducationCourse course = courseRepository.findById(request.courseId())
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course not found: " + request.courseId()));

        // Get fee breakdown from the active fee structure
        EnquiryFeeBreakdownDto breakdown = feeStructureService.getFeeBreakdown(request.courseId(), tenantId);

        BigDecimal genericTotal = breakdown.genericTotal();

        // Determine additional fee based on student type
        BigDecimal additionalFee;
        if (request.studentType() == StudentType.DAY_SCHOLAR) {
            additionalFee = breakdown.transportationFee();
        } else {
            // HOSTELER
            additionalFee = breakdown.hostelFee();
        }

        BigDecimal totalFees = genericTotal.add(additionalFee);

        StudentEnquiry enquiry = new StudentEnquiry(
                tenantId,
                request.studentName(),
                request.email(),
                request.phone(),
                program,
                course,
                request.studentType(),
                genericTotal,
                additionalFee,
                totalFees,
                request.academicYear()
        );

        StudentEnquiry saved = enquiryRepository.save(enquiry);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentEnquiryDto> listEnquiries(String tenantId) {
        return enquiryRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentEnquiryDto getEnquiry(UUID id, String tenantId) {
        StudentEnquiry enquiry = enquiryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enquiry not found: " + id));
        return toDto(enquiry);
    }

    private StudentEnquiryDto toDto(StudentEnquiry enquiry) {
        return new StudentEnquiryDto(
                enquiry.getId(),
                enquiry.getStudentName(),
                enquiry.getEmail(),
                enquiry.getPhone(),
                enquiry.getProgram().getId(),
                enquiry.getProgram().getName(),
                enquiry.getCourse().getId(),
                enquiry.getCourse().getName(),
                enquiry.getStudentType(),
                enquiry.getGenericTotal(),
                enquiry.getAdditionalFee(),
                enquiry.getTotalFees(),
                enquiry.getAcademicYear(),
                enquiry.getStatus(),
                enquiry.getEnquiryDate()
        );
    }
}

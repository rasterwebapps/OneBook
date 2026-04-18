package com.nexus.onebook.education.service;

import com.nexus.onebook.education.dto.*;
import com.nexus.onebook.education.model.*;
import com.nexus.onebook.education.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing fee structures and computing fee breakdowns.
 * A fee structure links a course to an academic year, with individual fee type amounts.
 */
@Service
public class FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;
    private final FeeStructureItemRepository feeStructureItemRepository;
    private final EducationCourseRepository courseRepository;
    private final FeeTypeRepository feeTypeRepository;

    public FeeStructureService(
            FeeStructureRepository feeStructureRepository,
            FeeStructureItemRepository feeStructureItemRepository,
            EducationCourseRepository courseRepository,
            FeeTypeRepository feeTypeRepository) {
        this.feeStructureRepository = feeStructureRepository;
        this.feeStructureItemRepository = feeStructureItemRepository;
        this.courseRepository = courseRepository;
        this.feeTypeRepository = feeTypeRepository;
    }

    /**
     * Retrieves the active fee structure for a given course, including all items and computed totals.
     */
    @Transactional(readOnly = true)
    public FeeStructureDto getFeeStructureByCourse(UUID courseId, String tenantId) {
        FeeStructure structure = feeStructureRepository
                .findByCourseIdAndTenantIdAndIsActiveTrue(courseId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active fee structure found for courseId: " + courseId));

        List<FeeStructureItem> items = feeStructureItemRepository
                .findAllByFeeStructureIdAndTenantId(structure.getId(), tenantId);

        return buildFeeStructureDto(structure, items);
    }

    /**
     * Creates a new fee structure or replaces the existing active one for the given course.
     * Any existing active structure is deactivated before the new one is saved.
     */
    @Transactional
    public FeeStructureDto createOrUpdateFeeStructure(CreateFeeStructureRequest request, String tenantId) {
        EducationCourse course = courseRepository.findById(request.courseId())
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course not found: " + request.courseId()));

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Fee structure must contain at least one item");
        }

        // Deactivate any existing active structure for this course
        feeStructureRepository.findByCourseIdAndTenantIdAndIsActiveTrue(request.courseId(), tenantId)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    feeStructureRepository.save(existing);
                });

        // Create the new fee structure
        FeeStructure structure = new FeeStructure(tenantId, course, request.academicYear());
        FeeStructure savedStructure = feeStructureRepository.save(structure);

        // Create fee structure items
        List<FeeStructureItem> items = new ArrayList<>();
        for (FeeStructureItemRequest itemRequest : request.items()) {
            FeeType feeType = feeTypeRepository.findById(itemRequest.feeTypeId())
                    .filter(ft -> ft.getTenantId().equals(tenantId))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "FeeType not found: " + itemRequest.feeTypeId()));

            items.add(new FeeStructureItem(tenantId, savedStructure, feeType, itemRequest.amount()));
        }

        List<FeeStructureItem> savedItems = feeStructureItemRepository.saveAll(items);
        return buildFeeStructureDto(savedStructure, savedItems);
    }

    /**
     * Computes the fee breakdown for enquiry creation:
     * - genericTotal: sum of all GENERIC fee items
     * - hostelFee: amount of the HOSTEL additional fee item (0 if absent)
     * - transportationFee: amount of the TRANSPORTATION additional fee item (0 if absent)
     */
    @Transactional(readOnly = true)
    public EnquiryFeeBreakdownDto getFeeBreakdown(UUID courseId, String tenantId) {
        FeeStructure structure = feeStructureRepository
                .findByCourseIdAndTenantIdAndIsActiveTrue(courseId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active fee structure found for courseId: " + courseId));

        List<FeeStructureItem> items = feeStructureItemRepository
                .findAllByFeeStructureIdAndTenantId(structure.getId(), tenantId);

        BigDecimal genericTotal = items.stream()
                .filter(i -> i.getFeeType().getCategory() == FeeCategory.GENERIC)
                .map(FeeStructureItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal hostelFee = items.stream()
                .filter(i -> i.getFeeType().getCategory() == FeeCategory.ADDITIONAL
                        && i.getFeeType().getAdditionalType() == AdditionalFeeType.HOSTEL)
                .map(FeeStructureItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal transportationFee = items.stream()
                .filter(i -> i.getFeeType().getCategory() == FeeCategory.ADDITIONAL
                        && i.getFeeType().getAdditionalType() == AdditionalFeeType.TRANSPORTATION)
                .map(FeeStructureItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EnquiryFeeBreakdownDto(genericTotal, hostelFee, transportationFee);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private FeeStructureDto buildFeeStructureDto(FeeStructure structure, List<FeeStructureItem> items) {
        List<FeeStructureItemDto> itemDtos = items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        BigDecimal genericTotal = items.stream()
                .filter(i -> i.getFeeType().getCategory() == FeeCategory.GENERIC)
                .map(FeeStructureItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal hostelFee = items.stream()
                .filter(i -> i.getFeeType().getCategory() == FeeCategory.ADDITIONAL
                        && i.getFeeType().getAdditionalType() == AdditionalFeeType.HOSTEL)
                .map(FeeStructureItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal transportationFee = items.stream()
                .filter(i -> i.getFeeType().getCategory() == FeeCategory.ADDITIONAL
                        && i.getFeeType().getAdditionalType() == AdditionalFeeType.TRANSPORTATION)
                .map(FeeStructureItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FeeStructureDto(
                structure.getId(),
                structure.getCourse().getId(),
                structure.getAcademicYear(),
                structure.isActive(),
                itemDtos,
                genericTotal,
                hostelFee,
                transportationFee
        );
    }

    private FeeStructureItemDto toItemDto(FeeStructureItem item) {
        return new FeeStructureItemDto(
                item.getId(),
                item.getFeeType().getId(),
                item.getFeeType().getName(),
                item.getFeeType().getCategory(),
                item.getFeeType().getAdditionalType(),
                item.getAmount()
        );
    }
}

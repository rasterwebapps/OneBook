package com.nexus.onebook.education.service;

import com.nexus.onebook.education.dto.CreateFinalizationRequest;
import com.nexus.onebook.education.dto.EnquiryFeeBreakdownDto;
import com.nexus.onebook.education.dto.FeeFinalizationDto;
import com.nexus.onebook.education.model.EnquiryStatus;
import com.nexus.onebook.education.model.FeeFinalization;
import com.nexus.onebook.education.model.StudentEnquiry;
import com.nexus.onebook.education.repository.FeeFinalizationRepository;
import com.nexus.onebook.education.repository.StudentEnquiryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for finalizing student fee enquiries.
 * Applies a discount to the computed fees and marks the enquiry as FINALIZED.
 */
@Service
public class FeeFinalizationService {

    private final FeeFinalizationRepository finalizationRepository;
    private final StudentEnquiryRepository enquiryRepository;
    private final FeeStructureService feeStructureService;

    public FeeFinalizationService(
            FeeFinalizationRepository finalizationRepository,
            StudentEnquiryRepository enquiryRepository,
            FeeStructureService feeStructureService) {
        this.finalizationRepository = finalizationRepository;
        this.enquiryRepository = enquiryRepository;
        this.feeStructureService = feeStructureService;
    }

    /**
     * Finalizes a student enquiry:
     * 1. Loads the enquiry — throws if not found or not OPEN
     * 2. Validates discount is non-negative and does not exceed total fees
     * 3. Computes finalPayable = genericTotal + additionalFee - discountAmount
     * 4. Saves the finalization record and updates the enquiry status to FINALIZED
     */
    @Transactional
    public FeeFinalizationDto finalizeEnquiry(CreateFinalizationRequest request, String tenantId) {
        StudentEnquiry enquiry = enquiryRepository.findByIdAndTenantId(request.enquiryId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enquiry not found: " + request.enquiryId()));

        if (enquiry.getStatus() != EnquiryStatus.OPEN) {
            throw new IllegalStateException(
                    "Cannot finalize enquiry with status: " + enquiry.getStatus()
                    + ". Only OPEN enquiries can be finalized.");
        }

        BigDecimal genericTotal = enquiry.getGenericTotal();
        BigDecimal additionalFee = enquiry.getAdditionalFee();
        BigDecimal totalFees = genericTotal.add(additionalFee);
        BigDecimal discountAmount = request.discountAmount();

        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount must not be negative");
        }
        if (discountAmount.compareTo(totalFees) > 0) {
            throw new IllegalArgumentException(
                    "Discount amount (" + discountAmount + ") cannot exceed total fees (" + totalFees + ")");
        }

        BigDecimal finalPayable = totalFees.subtract(discountAmount);

        FeeFinalization finalization = new FeeFinalization(
                tenantId,
                enquiry,
                genericTotal,
                additionalFee,
                discountAmount,
                finalPayable,
                request.finalizedBy()
        );

        FeeFinalization saved = finalizationRepository.save(finalization);

        // Update enquiry status to FINALIZED
        enquiry.setStatus(EnquiryStatus.FINALIZED);
        enquiryRepository.save(enquiry);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public FeeFinalizationDto getFinalization(UUID enquiryId, String tenantId) {
        FeeFinalization finalization = finalizationRepository
                .findByEnquiryIdAndTenantId(enquiryId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Finalization not found for enquiryId: " + enquiryId));
        return toDto(finalization);
    }

    private FeeFinalizationDto toDto(FeeFinalization f) {
        return new FeeFinalizationDto(
                f.getId(),
                f.getEnquiry().getId(),
                f.getGenericTotal(),
                f.getAdditionalFee(),
                f.getDiscountAmount(),
                f.getFinalPayable(),
                f.getFinalizedBy(),
                f.getFinalizedAt()
        );
    }
}

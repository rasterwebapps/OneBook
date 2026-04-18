package com.nexus.onebook.education.service;

import com.nexus.onebook.education.dto.FeeTypeDto;
import com.nexus.onebook.education.model.FeeCategory;
import com.nexus.onebook.education.model.FeeType;
import com.nexus.onebook.education.repository.FeeTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing fee types (GENERIC and ADDITIONAL).
 */
@Service
public class FeeTypeService {

    private final FeeTypeRepository feeTypeRepository;

    public FeeTypeService(FeeTypeRepository feeTypeRepository) {
        this.feeTypeRepository = feeTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<FeeTypeDto> listFeeTypes(String tenantId) {
        return feeTypeRepository.findAllByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeeTypeDto> listFeeTypesByCategory(FeeCategory category, String tenantId) {
        return feeTypeRepository.findAllByCategoryAndTenantIdAndIsActiveTrue(category, tenantId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private FeeTypeDto toDto(FeeType feeType) {
        return new FeeTypeDto(
                feeType.getId(),
                feeType.getName(),
                feeType.getCategory(),
                feeType.getAdditionalType(),
                feeType.isActive()
        );
    }
}

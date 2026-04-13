package com.nexus.onebook.intelligence.repository;

import com.nexus.onebook.intelligence.model.HoldingType;
import com.nexus.onebook.intelligence.model.InvestmentHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentHoldingRepository extends JpaRepository<InvestmentHolding, Long> {
    List<InvestmentHolding> findByTenantId(String tenantId);

    Optional<InvestmentHolding> findByTenantIdAndSymbol(String tenantId, String symbol);

    List<InvestmentHolding> findByTenantIdAndHoldingType(String tenantId, HoldingType holdingType);
}

package com.nexus.onebook.intelligence.repository;

import com.nexus.onebook.intelligence.model.DigitalAsset;
import com.nexus.onebook.intelligence.model.DigitalAssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DigitalAssetRepository extends JpaRepository<DigitalAsset, Long> {
    List<DigitalAsset> findByTenantId(String tenantId);

    Optional<DigitalAsset> findByTenantIdAndSymbol(String tenantId, String symbol);

    List<DigitalAsset> findByTenantIdAndAssetType(String tenantId, DigitalAssetType assetType);
}

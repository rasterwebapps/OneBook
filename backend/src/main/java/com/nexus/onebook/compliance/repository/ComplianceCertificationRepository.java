package com.nexus.onebook.compliance.repository;

import com.nexus.onebook.compliance.model.ComplianceCertification;
import com.nexus.onebook.compliance.model.CertificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceCertificationRepository extends JpaRepository<ComplianceCertification, Long> {

    List<ComplianceCertification> findByTenantId(String tenantId);

    List<ComplianceCertification> findByTenantIdAndStatus(String tenantId, CertificationStatus status);

    Optional<ComplianceCertification> findByTenantIdAndCertificationName(
            String tenantId, String certificationName);
}

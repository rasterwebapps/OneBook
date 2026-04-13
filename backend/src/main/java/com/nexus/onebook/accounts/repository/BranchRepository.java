package com.nexus.onebook.accounts.repository;

import com.nexus.onebook.accounts.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByTenantId(String tenantId);

    List<Branch> findByTenantIdAndEnterpriseId(String tenantId, Long enterpriseId);

    Optional<Branch> findByTenantIdAndEnterpriseIdAndCode(String tenantId, Long enterpriseId, String code);
}

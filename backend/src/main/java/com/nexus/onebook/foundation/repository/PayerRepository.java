package com.nexus.onebook.foundation.repository;

import com.nexus.onebook.foundation.model.Payer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayerRepository extends JpaRepository<Payer, Long> {
    List<Payer> findByTenantId(String tenantId);
    Optional<Payer> findByTenantIdAndCode(String tenantId, String code);
}

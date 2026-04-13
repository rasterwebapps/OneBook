package com.nexus.onebook.compliance.repository;

import com.nexus.onebook.compliance.model.TdsTcsEntry;
import com.nexus.onebook.compliance.model.TdsTcsStatus;
import com.nexus.onebook.compliance.model.TdsTcsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TdsTcsEntryRepository extends JpaRepository<TdsTcsEntry, Long> {
    List<TdsTcsEntry> findByTenantId(String tenantId);
    List<TdsTcsEntry> findByTenantIdAndEntryType(String tenantId, TdsTcsType entryType);
    List<TdsTcsEntry> findByTenantIdAndStatus(String tenantId, TdsTcsStatus status);
    List<TdsTcsEntry> findByTenantIdAndEntryTypeAndStatus(String tenantId, TdsTcsType entryType, TdsTcsStatus status);
}

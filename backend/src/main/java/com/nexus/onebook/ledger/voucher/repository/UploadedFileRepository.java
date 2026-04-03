package com.nexus.onebook.ledger.voucher.repository;

import com.nexus.onebook.ledger.voucher.model.UploadedFile;
import com.nexus.onebook.ledger.voucher.model.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findByTenantId(String tenantId);
    List<UploadedFile> findByTenantIdAndStatus(String tenantId, FileStatus status);
    List<UploadedFile> findByTenantIdAndUploadedBy(String tenantId, String uploadedBy);
}

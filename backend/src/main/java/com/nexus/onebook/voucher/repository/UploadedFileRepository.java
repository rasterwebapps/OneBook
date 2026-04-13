package com.nexus.onebook.voucher.repository;

import com.nexus.onebook.voucher.model.UploadedFile;
import com.nexus.onebook.voucher.model.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findByTenantId(String tenantId);
    List<UploadedFile> findByTenantIdAndStatus(String tenantId, FileStatus status);
    List<UploadedFile> findByTenantIdAndUploadedBy(String tenantId, String uploadedBy);
}

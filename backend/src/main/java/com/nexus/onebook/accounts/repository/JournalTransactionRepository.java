package com.nexus.onebook.accounts.repository;

import com.nexus.onebook.accounts.model.JournalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalTransactionRepository extends JpaRepository<JournalTransaction, Long> {

    @Query("SELECT DISTINCT t FROM JournalTransaction t LEFT JOIN FETCH t.entries WHERE t.transactionUuid = :uuid")
    Optional<JournalTransaction> findByTransactionUuid(@Param("uuid") UUID uuid);

    @Query("SELECT DISTINCT t FROM JournalTransaction t LEFT JOIN FETCH t.entries WHERE t.tenantId = :tenantId")
    List<JournalTransaction> findByTenantId(@Param("tenantId") String tenantId);
}

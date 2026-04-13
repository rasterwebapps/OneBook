package com.nexus.onebook.ledger.accounts.repository;

import com.nexus.onebook.ledger.accounts.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import com.nexus.onebook.ledger.accounts.model.AccountType;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByTransactionId(Long transactionId);

    /**
     * Retrieves all journal entries for a given tenant where the parent
     * transaction is posted, grouped by account. Used for trial balance.
     */
    @Query("SELECT e FROM JournalEntry e JOIN FETCH e.account " +
           "WHERE e.tenantId = :tenantId AND e.transaction.posted = true")
    List<JournalEntry> findPostedEntriesByTenantId(@Param("tenantId") String tenantId);

    /**
     * Retrieves posted entries for a tenant filtered by account type.
     * Used for financial reports (P&L, Balance Sheet).
     */
    @Query("SELECT e FROM JournalEntry e JOIN FETCH e.account a " +
           "WHERE e.tenantId = :tenantId AND e.transaction.posted = true " +
           "AND a.accountType = :accountType")
    List<JournalEntry> findPostedEntriesByTenantIdAndAccountType(
            @Param("tenantId") String tenantId,
            @Param("accountType") com.nexus.onebook.ledger.accounts.model.AccountType accountType);

    /**
     * Retrieves posted entries for a tenant filtered by branch (via account's cost center).
     * Used for branch-level reporting and consolidation.
     */
    @Query("SELECT e FROM JournalEntry e JOIN FETCH e.account a " +
           "WHERE e.tenantId = :tenantId AND e.transaction.posted = true " +
           "AND a.costCenter.branch.id = :branchId")
    List<JournalEntry> findPostedEntriesByTenantIdAndBranchId(
            @Param("tenantId") String tenantId,
            @Param("branchId") Long branchId);

    /**
     * Retrieves posted entries for a tenant with an optional date range filter.
     * When fromDate/toDate are provided, only entries whose parent transaction
     * falls within [fromDate, toDate] are returned. Used for date-bounded trial balance.
     */
    @Query("SELECT e FROM JournalEntry e JOIN FETCH e.account " +
           "WHERE e.tenantId = :tenantId AND e.transaction.posted = true " +
           "AND (CAST(:fromDate AS date) IS NULL OR e.transaction.transactionDate >= :fromDate) " +
           "AND (CAST(:toDate AS date) IS NULL OR e.transaction.transactionDate <= :toDate)")
    List<JournalEntry> findPostedEntriesByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}

package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.CashFlowForecast;
import com.nexus.onebook.ledger.accounts.model.AccountType;
import com.nexus.onebook.ledger.accounts.model.EntryType;
import com.nexus.onebook.ledger.accounts.model.JournalEntry;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.accounts.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ForecastingService {

    private final JournalEntryRepository entryRepository;

    public ForecastingService(JournalEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    public CashFlowForecast generateForecast(String tenantId) {
        List<JournalEntry> postedEntries = entryRepository.findPostedEntriesByTenantId(tenantId);

        if (postedEntries.isEmpty()) {
            return zeroCashFlowForecast(tenantId);
        }

        BigDecimal totalInflow = BigDecimal.ZERO;
        BigDecimal totalOutflow = BigDecimal.ZERO;
        BigDecimal currentCash = BigDecimal.ZERO;
        Instant earliest = null;
        Instant latest = null;

        for (JournalEntry entry : postedEntries) {
            LedgerAccount account = entry.getAccount();
            AccountType accountType = account.getAccountType();
            EntryType entryType = entry.getEntryType();
            BigDecimal amount = entry.getAmount();
            Instant createdAt = entry.getCreatedAt();

            // Track date span
            if (earliest == null || createdAt.isBefore(earliest)) {
                earliest = createdAt;
            }
            if (latest == null || createdAt.isAfter(latest)) {
                latest = createdAt;
            }

            // Inflows: REVENUE CREDIT entries
            if (accountType == AccountType.REVENUE && entryType == EntryType.CREDIT) {
                totalInflow = totalInflow.add(amount);
            }

            // Outflows: EXPENSE DEBIT entries
            if (accountType == AccountType.EXPENSE && entryType == EntryType.DEBIT) {
                totalOutflow = totalOutflow.add(amount);
            }

            // Current cash position: ASSET accounts containing "cash" or "bank"
            if (accountType == AccountType.ASSET) {
                String name = account.getAccountName().toLowerCase();
                if (name.contains("cash") || name.contains("bank")) {
                    if (entryType == EntryType.DEBIT) {
                        currentCash = currentCash.add(amount);
                    } else {
                        currentCash = currentCash.subtract(amount);
                    }
                }
            }
        }

        long numberOfDays = ChronoUnit.DAYS.between(earliest, latest);
        if (numberOfDays == 0) {
            return zeroCashFlowForecast(tenantId);
        }

        BigDecimal days = BigDecimal.valueOf(numberOfDays);
        BigDecimal avgDailyInflow = totalInflow.divide(days, 4, RoundingMode.HALF_UP);
        BigDecimal avgDailyOutflow = totalOutflow.divide(days, 4, RoundingMode.HALF_UP);
        BigDecimal dailyNet = avgDailyInflow.subtract(avgDailyOutflow);

        BigDecimal forecast30Day = currentCash.add(dailyNet.multiply(BigDecimal.valueOf(30)));
        BigDecimal forecast60Day = currentCash.add(dailyNet.multiply(BigDecimal.valueOf(60)));
        BigDecimal forecast90Day = currentCash.add(dailyNet.multiply(BigDecimal.valueOf(90)));

        String riskLevel;
        if (forecast30Day.compareTo(BigDecimal.ZERO) < 0) {
            riskLevel = "HIGH";
        } else if (forecast90Day.compareTo(BigDecimal.ZERO) < 0) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        return new CashFlowForecast(
                tenantId,
                currentCash,
                forecast30Day,
                forecast60Day,
                forecast90Day,
                avgDailyInflow,
                avgDailyOutflow,
                riskLevel,
                LocalDate.now()
        );
    }

    private CashFlowForecast zeroCashFlowForecast(String tenantId) {
        return new CashFlowForecast(
                tenantId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "LOW",
                LocalDate.now()
        );
    }
}

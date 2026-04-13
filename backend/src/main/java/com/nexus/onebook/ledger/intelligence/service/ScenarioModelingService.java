package com.nexus.onebook.ledger.intelligence.service;

import com.nexus.onebook.ledger.intelligence.dto.ScenarioRequest;
import com.nexus.onebook.ledger.intelligence.dto.ScenarioResult;
import com.nexus.onebook.ledger.accounts.model.AccountType;
import com.nexus.onebook.ledger.accounts.model.EntryType;
import com.nexus.onebook.ledger.accounts.model.JournalEntry;
import com.nexus.onebook.ledger.accounts.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ScenarioModelingService {

    private final JournalEntryRepository entryRepository;

    public ScenarioModelingService(JournalEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    public ScenarioResult runScenario(ScenarioRequest request) {
        List<JournalEntry> entries = entryRepository.findPostedEntriesByTenantId(request.tenantId());

        BigDecimal totalRevenue = entries.stream()
                .filter(e -> e.getAccount().getAccountType() == AccountType.REVENUE
                        && e.getEntryType() == EntryType.CREDIT)
                .map(JournalEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = entries.stream()
                .filter(e -> e.getAccount().getAccountType() == AccountType.EXPENSE
                        && e.getEntryType() == EntryType.DEBIT)
                .map(JournalEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal baselineNetIncome = totalRevenue.subtract(totalExpenses);

        BigDecimal projectedRevenue = totalRevenue.multiply(
                BigDecimal.ONE.add(request.revenueChangePercent()
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));

        BigDecimal projectedExpenses = totalExpenses.multiply(
                BigDecimal.ONE.add(request.expenseChangePercent()
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));

        if (request.interestRateChange() != null
                && request.interestRateChange().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal interestImpact = totalExpenses.multiply(request.interestRateChange())
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            projectedExpenses = projectedExpenses.add(interestImpact);
        }

        BigDecimal projectedNetIncome = projectedRevenue.subtract(projectedExpenses);
        BigDecimal projectedCashFlow = projectedNetIncome;
        BigDecimal impactOnCash = projectedNetIncome.subtract(baselineNetIncome);

        BigDecimal impactPercent = baselineNetIncome.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : impactOnCash.divide(baselineNetIncome, 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        String summary = String.format(
                "Scenario '%s': Revenue %+.1f%%, Expenses %+.1f%% → Net income changes by %s",
                request.scenarioName(),
                request.revenueChangePercent().doubleValue(),
                request.expenseChangePercent().doubleValue(),
                impactOnCash.setScale(2, RoundingMode.HALF_UP).toPlainString());

        return new ScenarioResult(
                request.tenantId(),
                request.scenarioName(),
                baselineNetIncome,
                projectedNetIncome,
                projectedCashFlow,
                impactOnCash,
                impactPercent,
                summary);
    }
}

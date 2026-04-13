package com.nexus.onebook.accounts.service;

import com.nexus.onebook.accounts.dto.BalanceSheetReport;
import com.nexus.onebook.accounts.dto.TrialBalanceLine;
import com.nexus.onebook.accounts.model.AccountType;
import com.nexus.onebook.accounts.model.EntryType;
import com.nexus.onebook.accounts.model.JournalEntry;
import com.nexus.onebook.accounts.model.LedgerAccount;
import com.nexus.onebook.accounts.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BalanceSheetService {

    private final JournalEntryRepository entryRepository;

    public BalanceSheetService(JournalEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    public BalanceSheetReport generateBalanceSheet(String tenantId) {
        List<JournalEntry> postedEntries = entryRepository.findPostedEntriesByTenantId(tenantId);

        Map<Long, AccountAggregator> assetMap = new LinkedHashMap<>();
        Map<Long, AccountAggregator> liabilityMap = new LinkedHashMap<>();
        Map<Long, AccountAggregator> equityMap = new LinkedHashMap<>();

        for (JournalEntry entry : postedEntries) {
            LedgerAccount account = entry.getAccount();
            Map<Long, AccountAggregator> targetMap = switch (account.getAccountType()) {
                case ASSET -> assetMap;
                case LIABILITY -> liabilityMap;
                case EQUITY -> equityMap;
                default -> null;
            };
            if (targetMap != null) {
                AccountAggregator agg = targetMap.computeIfAbsent(
                        account.getId(), id -> new AccountAggregator(account));
                accumulate(agg, entry);
            }
        }

        List<TrialBalanceLine> assetLines = buildLines(assetMap);
        List<TrialBalanceLine> liabilityLines = buildLines(liabilityMap);
        List<TrialBalanceLine> equityLines = buildLines(equityMap);

        // Assets: debit - credit; Liabilities/Equity: credit - debit
        BigDecimal totalAssets = assetMap.values().stream()
                .map(a -> a.totalDebits.subtract(a.totalCredits))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLiabilities = liabilityMap.values().stream()
                .map(a -> a.totalCredits.subtract(a.totalDebits))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEquity = equityMap.values().stream()
                .map(a -> a.totalCredits.subtract(a.totalDebits))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean balanced = totalAssets.compareTo(totalLiabilities.add(totalEquity)) == 0;

        return new BalanceSheetReport(tenantId, assetLines, liabilityLines, equityLines,
                totalAssets, totalLiabilities, totalEquity, balanced);
    }

    private void accumulate(AccountAggregator agg, JournalEntry entry) {
        if (entry.getEntryType() == EntryType.DEBIT) {
            agg.totalDebits = agg.totalDebits.add(entry.getAmount());
        } else {
            agg.totalCredits = agg.totalCredits.add(entry.getAmount());
        }
    }

    private List<TrialBalanceLine> buildLines(Map<Long, AccountAggregator> map) {
        List<TrialBalanceLine> lines = new ArrayList<>();
        for (AccountAggregator agg : map.values()) {
            lines.add(new TrialBalanceLine(
                    agg.account.getId(), agg.account.getAccountCode(),
                    agg.account.getAccountName(), agg.account.getAccountType().name(),
                    agg.totalDebits, agg.totalCredits));
        }
        return lines;
    }

    private static class AccountAggregator {
        final LedgerAccount account;
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        AccountAggregator(LedgerAccount account) { this.account = account; }
    }
}

package com.nexus.onebook.ledger.accounts.service;

import com.nexus.onebook.ledger.accounts.dto.ProfitAndLossReport;
import com.nexus.onebook.ledger.accounts.dto.TrialBalanceLine;
import com.nexus.onebook.ledger.accounts.model.AccountType;
import com.nexus.onebook.ledger.accounts.model.EntryType;
import com.nexus.onebook.ledger.accounts.model.JournalEntry;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.accounts.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfitAndLossService {

    private final JournalEntryRepository entryRepository;

    public ProfitAndLossService(JournalEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    public ProfitAndLossReport generateProfitAndLoss(String tenantId) {
        List<JournalEntry> postedEntries = entryRepository.findPostedEntriesByTenantId(tenantId);

        Map<Long, AccountAggregator> revenueMap = new LinkedHashMap<>();
        Map<Long, AccountAggregator> expenseMap = new LinkedHashMap<>();

        for (JournalEntry entry : postedEntries) {
            LedgerAccount account = entry.getAccount();
            if (account.getAccountType() == AccountType.REVENUE) {
                AccountAggregator agg = revenueMap.computeIfAbsent(
                        account.getId(), id -> new AccountAggregator(account));
                accumulate(agg, entry);
            } else if (account.getAccountType() == AccountType.EXPENSE) {
                AccountAggregator agg = expenseMap.computeIfAbsent(
                        account.getId(), id -> new AccountAggregator(account));
                accumulate(agg, entry);
            }
        }

        List<TrialBalanceLine> revenueLines = buildLines(revenueMap);
        List<TrialBalanceLine> expenseLines = buildLines(expenseMap);

        // Revenue natural balance is credit; Expense natural balance is debit
        BigDecimal totalRevenue = revenueMap.values().stream()
                .map(a -> a.totalCredits.subtract(a.totalDebits))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenseMap.values().stream()
                .map(a -> a.totalDebits.subtract(a.totalCredits))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netIncome = totalRevenue.subtract(totalExpenses);

        return new ProfitAndLossReport(tenantId, revenueLines, expenseLines,
                totalRevenue, totalExpenses, netIncome);
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

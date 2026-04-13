package com.nexus.onebook.ledger.accounts.service;

import com.nexus.onebook.ledger.accounts.dto.CashFlowLine;
import com.nexus.onebook.ledger.accounts.dto.CashFlowReport;
import com.nexus.onebook.ledger.accounts.model.AccountType;
import com.nexus.onebook.ledger.accounts.model.EntryType;
import com.nexus.onebook.ledger.accounts.model.JournalEntry;
import com.nexus.onebook.ledger.accounts.model.LedgerAccount;
import com.nexus.onebook.ledger.accounts.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CashFlowService {

    private final JournalEntryRepository entryRepository;

    public CashFlowService(JournalEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    public CashFlowReport generateCashFlow(String tenantId) {
        List<JournalEntry> postedEntries = entryRepository.findPostedEntriesByTenantId(tenantId);

        List<CashFlowLine> operatingActivities = new ArrayList<>();
        List<CashFlowLine> investingActivities = new ArrayList<>();
        List<CashFlowLine> financingActivities = new ArrayList<>();

        BigDecimal netRevenue = BigDecimal.ZERO;
        BigDecimal netExpenses = BigDecimal.ZERO;
        BigDecimal netInvesting = BigDecimal.ZERO;
        BigDecimal netFinancing = BigDecimal.ZERO;

        for (JournalEntry entry : postedEntries) {
            LedgerAccount account = entry.getAccount();
            BigDecimal signedAmount = entry.getEntryType() == EntryType.DEBIT
                    ? entry.getAmount() : entry.getAmount().negate();

            switch (account.getAccountType()) {
                case REVENUE -> netRevenue = netRevenue.add(signedAmount.negate());
                case EXPENSE -> netExpenses = netExpenses.add(signedAmount);
                case ASSET -> {
                    // Non-cash asset changes represent investing activities
                    String name = account.getAccountName().toLowerCase();
                    if (!name.contains("cash") && !name.contains("bank")) {
                        netInvesting = netInvesting.add(signedAmount.negate());
                    }
                }
                case LIABILITY, EQUITY -> netFinancing = netFinancing.add(signedAmount.negate());
            }
        }

        BigDecimal netOperating = netRevenue.subtract(netExpenses);
        operatingActivities.add(new CashFlowLine("Net Income", netOperating));
        investingActivities.add(new CashFlowLine("Capital Expenditures / Asset Changes", netInvesting));
        financingActivities.add(new CashFlowLine("Financing Activities", netFinancing));

        BigDecimal netCashChange = netOperating.add(netInvesting).add(netFinancing);

        return new CashFlowReport(tenantId,
                operatingActivities, investingActivities, financingActivities,
                netOperating, netInvesting, netFinancing, netCashChange);
    }
}

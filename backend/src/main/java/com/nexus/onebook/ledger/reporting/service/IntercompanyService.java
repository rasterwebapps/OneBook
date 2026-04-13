package com.nexus.onebook.ledger.reporting.service;

import com.nexus.onebook.ledger.accounts.dto.*;
import com.nexus.onebook.ledger.auditor.dto.*;
import com.nexus.onebook.ledger.banking.dto.*;
import com.nexus.onebook.ledger.clientaccount.dto.*;
import com.nexus.onebook.ledger.compliance.dto.*;
import com.nexus.onebook.ledger.credit.dto.*;
import com.nexus.onebook.ledger.currency.dto.*;
import com.nexus.onebook.ledger.entitlement.dto.*;
import com.nexus.onebook.ledger.fixedasset.dto.*;
import com.nexus.onebook.ledger.intelligence.dto.*;
import com.nexus.onebook.ledger.inventory.dto.*;
import com.nexus.onebook.ledger.operations.dto.*;
import com.nexus.onebook.ledger.payroll.dto.*;
import com.nexus.onebook.ledger.reporting.dto.*;
import com.nexus.onebook.ledger.tenant.dto.*;
import com.nexus.onebook.ledger.accounts.model.*;
import com.nexus.onebook.ledger.auditor.model.*;
import com.nexus.onebook.ledger.banking.model.*;
import com.nexus.onebook.ledger.clientaccount.model.*;
import com.nexus.onebook.ledger.compliance.model.*;
import com.nexus.onebook.ledger.credit.model.*;
import com.nexus.onebook.ledger.currency.model.*;
import com.nexus.onebook.ledger.entitlement.model.*;
import com.nexus.onebook.ledger.fixedasset.model.*;
import com.nexus.onebook.ledger.foundation.model.*;
import com.nexus.onebook.ledger.intelligence.model.*;
import com.nexus.onebook.ledger.inventory.model.*;
import com.nexus.onebook.ledger.operations.model.*;
import com.nexus.onebook.ledger.payroll.model.*;
import com.nexus.onebook.ledger.reporting.model.*;
import com.nexus.onebook.ledger.tenant.model.*;
import com.nexus.onebook.ledger.accounts.repository.BranchRepository;
import com.nexus.onebook.ledger.reporting.repository.IntercompanyEliminationRepository;
import com.nexus.onebook.ledger.accounts.repository.JournalTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.nexus.onebook.ledger.accounts.service.BalanceSheetService;
import com.nexus.onebook.ledger.accounts.service.ProfitAndLossService;

@Service
public class IntercompanyService {

    private final IntercompanyEliminationRepository eliminationRepository;
    private final JournalTransactionRepository transactionRepository;
    private final BranchRepository branchRepository;
    private final BalanceSheetService balanceSheetService;
    private final ProfitAndLossService profitAndLossService;

    public IntercompanyService(IntercompanyEliminationRepository eliminationRepository,
                                JournalTransactionRepository transactionRepository,
                                BranchRepository branchRepository,
                                BalanceSheetService balanceSheetService,
                                ProfitAndLossService profitAndLossService) {
        this.eliminationRepository = eliminationRepository;
        this.transactionRepository = transactionRepository;
        this.branchRepository = branchRepository;
        this.balanceSheetService = balanceSheetService;
        this.profitAndLossService = profitAndLossService;
    }

    @Transactional
    public IntercompanyElimination recordIntercompanyTransaction(
            String tenantId, Long sourceBranchId, Long targetBranchId,
            Long journalTransactionId, BigDecimal amount) {

        if (sourceBranchId.equals(targetBranchId)) {
            throw new IllegalArgumentException("Source and target branches must be different");
        }

        Branch sourceBranch = branchRepository.findById(sourceBranchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source branch not found: " + sourceBranchId));

        Branch targetBranch = branchRepository.findById(targetBranchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Target branch not found: " + targetBranchId));

        JournalTransaction txn = transactionRepository.findById(journalTransactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Journal transaction not found: " + journalTransactionId));

        IntercompanyElimination elimination = new IntercompanyElimination(
                tenantId, sourceBranch, targetBranch, txn, amount);

        return eliminationRepository.save(elimination);
    }

    @Transactional
    public IntercompanyElimination eliminateTransaction(Long eliminationId) {
        IntercompanyElimination elimination = eliminationRepository.findById(eliminationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Intercompany elimination not found: " + eliminationId));

        if (elimination.isEliminated()) {
            throw new IllegalArgumentException("Transaction already eliminated: " + eliminationId);
        }

        elimination.setEliminated(true);
        elimination.setEliminationDate(LocalDate.now());

        return eliminationRepository.save(elimination);
    }

    @Transactional(readOnly = true)
    public ConsolidatedReport generateConsolidatedReport(String tenantId) {
        BalanceSheetReport consolidatedBS = balanceSheetService.generateBalanceSheet(tenantId);
        ProfitAndLossReport consolidatedPL = profitAndLossService.generateProfitAndLoss(tenantId);

        List<IntercompanyElimination> eliminations = eliminationRepository.findByTenantId(tenantId);

        List<IntercompanyEliminationLine> eliminationLines = new ArrayList<>();
        BigDecimal totalEliminationAmount = BigDecimal.ZERO;

        for (IntercompanyElimination elim : eliminations) {
            eliminationLines.add(new IntercompanyEliminationLine(
                    elim.getSourceBranch().getId(),
                    elim.getSourceBranch().getName(),
                    elim.getTargetBranch().getId(),
                    elim.getTargetBranch().getName(),
                    elim.getEliminationAmount(),
                    elim.isEliminated()));

            if (elim.isEliminated()) {
                totalEliminationAmount = totalEliminationAmount.add(elim.getEliminationAmount());
            }
        }

        return new ConsolidatedReport(tenantId, consolidatedBS, consolidatedPL,
                eliminationLines, totalEliminationAmount);
    }

    @Transactional(readOnly = true)
    public List<IntercompanyElimination> getPendingEliminations(String tenantId) {
        return eliminationRepository.findByTenantIdAndEliminated(tenantId, false);
    }
}

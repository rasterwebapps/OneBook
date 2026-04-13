package com.nexus.onebook.reporting.service;

import com.nexus.onebook.accounts.dto.*;
import com.nexus.onebook.auditor.dto.*;
import com.nexus.onebook.banking.dto.*;
import com.nexus.onebook.clientaccount.dto.*;
import com.nexus.onebook.compliance.dto.*;
import com.nexus.onebook.credit.dto.*;
import com.nexus.onebook.currency.dto.*;
import com.nexus.onebook.entitlement.dto.*;
import com.nexus.onebook.fixedasset.dto.*;
import com.nexus.onebook.intelligence.dto.*;
import com.nexus.onebook.inventory.dto.*;
import com.nexus.onebook.operations.dto.*;
import com.nexus.onebook.payroll.dto.*;
import com.nexus.onebook.reporting.dto.*;
import com.nexus.onebook.tenant.dto.*;
import com.nexus.onebook.accounts.model.*;
import com.nexus.onebook.auditor.model.*;
import com.nexus.onebook.banking.model.*;
import com.nexus.onebook.clientaccount.model.*;
import com.nexus.onebook.compliance.model.*;
import com.nexus.onebook.credit.model.*;
import com.nexus.onebook.currency.model.*;
import com.nexus.onebook.entitlement.model.*;
import com.nexus.onebook.fixedasset.model.*;
import com.nexus.onebook.foundation.model.*;
import com.nexus.onebook.intelligence.model.*;
import com.nexus.onebook.inventory.model.*;
import com.nexus.onebook.operations.model.*;
import com.nexus.onebook.payroll.model.*;
import com.nexus.onebook.reporting.model.*;
import com.nexus.onebook.tenant.model.*;
import com.nexus.onebook.accounts.repository.BranchRepository;
import com.nexus.onebook.reporting.repository.IntercompanyEliminationRepository;
import com.nexus.onebook.accounts.repository.JournalTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.nexus.onebook.accounts.service.BalanceSheetService;
import com.nexus.onebook.accounts.service.ProfitAndLossService;

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

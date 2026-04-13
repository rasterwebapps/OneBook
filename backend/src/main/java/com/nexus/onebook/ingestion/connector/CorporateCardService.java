package com.nexus.onebook.ingestion.connector;

import com.nexus.onebook.ingestion.dto.CardTransactionRequest;
import com.nexus.onebook.ingestion.model.CardTransaction;
import com.nexus.onebook.ingestion.repository.CardTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Corporate Card API Integration.
 * Syncs transactions instantly from corporate cards (Ramp/Brex equivalents).
 * Transactions are stored and can later be posted to the ledger via the ingestion pipeline.
 */
@Service
public class CorporateCardService {

    private final CardTransactionRepository cardTransactionRepository;

    public CorporateCardService(CardTransactionRepository cardTransactionRepository) {
        this.cardTransactionRepository = cardTransactionRepository;
    }

    /**
     * Syncs a corporate card transaction. Duplicate external IDs are rejected.
     *
     * @param request the card transaction data
     * @return the persisted CardTransaction
     */
    @Transactional
    public CardTransaction syncTransaction(CardTransactionRequest request) {
        // Check for duplicate
        cardTransactionRepository.findByTenantIdAndExternalId(request.tenantId(), request.externalId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Duplicate card transaction: " + request.externalId());
                });

        CardTransaction txn = new CardTransaction(
                request.tenantId(),
                request.externalId(),
                request.merchantName(),
                request.amount(),
                request.transactionDate()
        );

        txn.setCardLastFour(request.cardLastFour());
        txn.setCurrency(request.currency());
        txn.setCategory(request.category());
        txn.setDescription(request.description());

        if (request.metadata() != null) {
            txn.setMetadata(request.metadata());
        }

        return cardTransactionRepository.save(txn);
    }

    /**
     * Returns all unposted card transactions for a tenant.
     */
    @Transactional(readOnly = true)
    public List<CardTransaction> getUnpostedTransactions(String tenantId) {
        return cardTransactionRepository.findByTenantIdAndPostedFalse(tenantId);
    }
}

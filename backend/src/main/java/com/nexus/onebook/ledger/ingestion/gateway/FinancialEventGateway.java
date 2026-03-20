package com.nexus.onebook.ledger.ingestion.gateway;

import com.nexus.onebook.ledger.ingestion.mapper.UniversalMapper;
import com.nexus.onebook.ledger.ingestion.model.AdapterType;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterStatus;
import com.nexus.onebook.ledger.payment.model.PaymentRegisterEntry;
import com.nexus.onebook.ledger.payment.repository.PaymentRegisterRepository;
import com.nexus.onebook.ledger.dto.JournalTransactionRequest;
import com.nexus.onebook.ledger.model.JournalTransaction;
import com.nexus.onebook.ledger.service.JournalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Financial Event Gateway — the central entry point for all external data ingestion.
 * Routes incoming payloads through the correct adapter, maps the normalised event
 * into a double-entry journal transaction, and posts it to the ledger.
 */
@Service
public class FinancialEventGateway {

    private final AdapterRegistry adapterRegistry;
    private final PaymentRegisterRepository registerRepository;
    private final UniversalMapper universalMapper;
    private final JournalService journalService;

    public FinancialEventGateway(AdapterRegistry adapterRegistry,
                                 PaymentRegisterRepository registerRepository,
                                 UniversalMapper universalMapper,
                                 JournalService journalService) {
        this.adapterRegistry = adapterRegistry;
        this.registerRepository = registerRepository;
        this.universalMapper = universalMapper;
        this.journalService = journalService;
    }

    /**
     * Ingests a raw payload through the specified adapter, persists the normalised
     * event, maps it to a journal transaction, and posts it to the ledger.
     *
     * @param tenantId    the tenant context
     * @param adapterType the adapter to use for parsing
     * @param rawPayload  the raw message from the external system
     * @return the persisted PaymentRegisterEntry with its final status
     */
    @Transactional
    public PaymentRegisterEntry ingest(String tenantId, AdapterType adapterType, String rawPayload) {
        FinancialEventAdapter adapter = adapterRegistry.getAdapter(adapterType);

        // 1. Parse
        PaymentRegisterEntry event;
        try {
            event = adapter.parse(tenantId, rawPayload);
            event.setStatus(PaymentRegisterStatus.VALIDATED);
        } catch (Exception e) {
            PaymentRegisterEntry failedEvent = new PaymentRegisterEntry(tenantId, adapterType, "PARSE_ERROR");
            failedEvent.setRawPayload(rawPayload);
            failedEvent.setStatus(PaymentRegisterStatus.FAILED);
            failedEvent.setErrorMessage(e.getMessage());
            return registerRepository.save(failedEvent);
        }

        // 2. Persist the validated event
        event = registerRepository.save(event);

        // External app payment requests skip auto-posting: journal entries
        // are created later when the payment batch is approved via PaymentBatchService.
        if (adapterType == AdapterType.EXTERNAL_APP) {
            return event;
        }

        // 3. Map to journal transaction
        try {
            JournalTransactionRequest journalRequest = universalMapper.mapToJournalRequest(event);

            // 4. Post to the ledger
            JournalTransaction posted = journalService.createTransaction(journalRequest);
            event.setStatus(PaymentRegisterStatus.POSTED);
            event.setSourceReference(posted.getTransactionUuid().toString());
        } catch (Exception e) {
            event.setStatus(PaymentRegisterStatus.FAILED);
            event.setErrorMessage("Mapping/posting failed: " + e.getMessage());
        }

        return registerRepository.save(event);
    }

    /**
     * Ingests and validates a raw payload without posting (dry run).
     * Useful for testing adapter parsing and validation.
     *
     * @param tenantId    the tenant context
     * @param adapterType the adapter to use for parsing
     * @param rawPayload  the raw message from the external system
     * @return the validated (but not posted) FinancialEvent
     */
    @Transactional
    public PaymentRegisterEntry ingestValidateOnly(String tenantId, AdapterType adapterType, String rawPayload) {
        FinancialEventAdapter adapter = adapterRegistry.getAdapter(adapterType);

        PaymentRegisterEntry event = adapter.parse(tenantId, rawPayload);
        event.setStatus(PaymentRegisterStatus.VALIDATED);
        return registerRepository.save(event);
    }
}

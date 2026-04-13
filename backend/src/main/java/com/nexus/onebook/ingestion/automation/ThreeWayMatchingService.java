package com.nexus.onebook.ingestion.automation;

import com.nexus.onebook.ingestion.dto.ThreeWayMatchResult;
import com.nexus.onebook.ingestion.model.GoodsReceipt;
import com.nexus.onebook.ingestion.model.MatchStatus;
import com.nexus.onebook.ingestion.model.PurchaseOrder;
import com.nexus.onebook.ingestion.model.VendorInvoice;
import com.nexus.onebook.ingestion.repository.GoodsReceiptRepository;
import com.nexus.onebook.ingestion.repository.PurchaseOrderRepository;
import com.nexus.onebook.ingestion.repository.VendorInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated 3-Way Matching service.
 * Verifies that the Purchase Order, Goods Receipt, and Vendor Invoice
 * match before allowing payment. All three documents must agree on
 * the PO number and the total amounts must be within tolerance.
 */
@Service
public class ThreeWayMatchingService {

    /** Tolerance for amount comparisons (0.01 = 1 cent). */
    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    private final PurchaseOrderRepository poRepository;
    private final GoodsReceiptRepository grRepository;
    private final VendorInvoiceRepository invoiceRepository;

    public ThreeWayMatchingService(PurchaseOrderRepository poRepository,
                                   GoodsReceiptRepository grRepository,
                                   VendorInvoiceRepository invoiceRepository) {
        this.poRepository = poRepository;
        this.grRepository = grRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Performs 3-way matching for the given PO number within a tenant.
     *
     * @param tenantId the tenant context
     * @param poNumber the purchase order number to match
     * @return the match result with status and any discrepancies
     */
    @Transactional
    public ThreeWayMatchResult match(String tenantId, String poNumber) {
        List<String> discrepancies = new ArrayList<>();

        // Find the three documents
        PurchaseOrder po = poRepository.findByTenantIdAndPoNumber(tenantId, poNumber)
                .orElse(null);
        GoodsReceipt gr = grRepository.findByTenantIdAndPoNumber(tenantId, poNumber)
                .orElse(null);
        VendorInvoice invoice = invoiceRepository.findByTenantIdAndPoNumber(tenantId, poNumber)
                .orElse(null);

        if (po == null) {
            discrepancies.add("Purchase Order not found: " + poNumber);
        }
        if (gr == null) {
            discrepancies.add("Goods Receipt not found for PO: " + poNumber);
        }
        if (invoice == null) {
            discrepancies.add("Vendor Invoice not found for PO: " + poNumber);
        }

        // If any document is missing, return PARTIAL match
        if (po == null || gr == null || invoice == null) {
            return new ThreeWayMatchResult(
                    MatchStatus.PARTIAL,
                    poNumber,
                    po != null ? po.getTotalAmount() : null,
                    gr != null ? gr.getTotalAmount() : null,
                    invoice != null ? invoice.getTotalAmount() : null,
                    false,
                    discrepancies,
                    "Incomplete document set — cannot perform 3-way match"
            );
        }

        // Compare amounts
        boolean poGrMatch = isWithinTolerance(po.getTotalAmount(), gr.getTotalAmount());
        boolean poInvMatch = isWithinTolerance(po.getTotalAmount(), invoice.getTotalAmount());
        boolean grInvMatch = isWithinTolerance(gr.getTotalAmount(), invoice.getTotalAmount());

        if (!poGrMatch) {
            discrepancies.add("PO amount (" + po.getTotalAmount().toPlainString()
                    + ") does not match GR amount (" + gr.getTotalAmount().toPlainString() + ")");
        }
        if (!poInvMatch) {
            discrepancies.add("PO amount (" + po.getTotalAmount().toPlainString()
                    + ") does not match Invoice amount (" + invoice.getTotalAmount().toPlainString() + ")");
        }
        if (!grInvMatch) {
            discrepancies.add("GR amount (" + gr.getTotalAmount().toPlainString()
                    + ") does not match Invoice amount (" + invoice.getTotalAmount().toPlainString() + ")");
        }

        boolean allMatch = poGrMatch && poInvMatch && grInvMatch;
        MatchStatus status = allMatch ? MatchStatus.MATCHED : MatchStatus.MISMATCHED;

        // Update the invoice match status
        invoice.setMatchStatus(status);
        invoiceRepository.save(invoice);

        return new ThreeWayMatchResult(
                status,
                poNumber,
                po.getTotalAmount(),
                gr.getTotalAmount(),
                invoice.getTotalAmount(),
                allMatch,
                discrepancies,
                allMatch ? "All three documents match — payment authorised"
                         : "Discrepancies found — manual review required"
        );
    }

    private boolean isWithinTolerance(BigDecimal a, BigDecimal b) {
        return a.subtract(b).abs().compareTo(TOLERANCE) <= 0;
    }
}

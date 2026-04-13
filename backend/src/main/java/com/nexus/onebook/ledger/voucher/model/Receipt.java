package com.nexus.onebook.ledger.voucher.model;

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
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "receipt_number", nullable = false, length = 50)
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private Payer payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_bank_account_id")
    private PayerBankAccount payerBankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id")
    private Payee payee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_bank_account_id")
    private PayeeBankAccount payeeBankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_ledger_account_id")
    private LedgerAccount fromLedgerAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_ledger_account_id")
    private LedgerAccount toLedgerAccount;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReceiptStatus status;

    @Column(name = "receipt_date")
    private Instant receiptDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Receipt() {}

    public Receipt(String tenantId, String receiptNumber, BigDecimal amount, String createdBy) {
        this.tenantId = tenantId;
        this.receiptNumber = receiptNumber;
        this.amount = amount;
        this.createdBy = createdBy;
        this.status = ReceiptStatus.CREATED;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public Voucher getVoucher() { return voucher; }
    public void setVoucher(Voucher voucher) { this.voucher = voucher; }

    public Payer getPayer() { return payer; }
    public void setPayer(Payer payer) { this.payer = payer; }

    public PayerBankAccount getPayerBankAccount() { return payerBankAccount; }
    public void setPayerBankAccount(PayerBankAccount payerBankAccount) { this.payerBankAccount = payerBankAccount; }

    public Payee getPayee() { return payee; }
    public void setPayee(Payee payee) { this.payee = payee; }

    public PayeeBankAccount getPayeeBankAccount() { return payeeBankAccount; }
    public void setPayeeBankAccount(PayeeBankAccount payeeBankAccount) { this.payeeBankAccount = payeeBankAccount; }

    public LedgerAccount getFromLedgerAccount() { return fromLedgerAccount; }
    public void setFromLedgerAccount(LedgerAccount fromLedgerAccount) { this.fromLedgerAccount = fromLedgerAccount; }

    public LedgerAccount getToLedgerAccount() { return toLedgerAccount; }
    public void setToLedgerAccount(LedgerAccount toLedgerAccount) { this.toLedgerAccount = toLedgerAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public ReceiptStatus getStatus() { return status; }
    public void setStatus(ReceiptStatus status) { this.status = status; }

    public Instant getReceiptDate() { return receiptDate; }
    public void setReceiptDate(Instant receiptDate) { this.receiptDate = receiptDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

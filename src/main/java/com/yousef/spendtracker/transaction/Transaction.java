package com.yousef.spendtracker.transaction;

import com.yousef.spendtracker.ingest.Direction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "booked_on", nullable = false)
    private LocalDate bookedOn;

    @Column(name = "raw_description", nullable = false)
    private String rawDescription;

    @Column(name = "normalised_description", nullable = false)
    private String normalisedDescription;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorised_by", nullable = false)
    private CategorisedBy categorisedBy = CategorisedBy.RULE;

    @Column(name = "is_internal_transfer", nullable = false)
    private boolean internalTransfer = false;

    @Column(nullable = false, length = 64, unique = true)
    private String fingerprint;

    @Column(name = "source_file_hash", nullable = false, length = 64)
    private String sourceFileHash;

    @Column(name = "ingested_at", nullable = false)
    private OffsetDateTime ingestedAt = OffsetDateTime.now();

    protected Transaction() {
        // JPA
    }

    public Transaction(Long accountId, LocalDate bookedOn, String rawDescription,
                        String normalisedDescription, BigDecimal amount, Direction direction,
                        String fingerprint, String sourceFileHash) {
        this.accountId = accountId;
        this.bookedOn = bookedOn;
        this.rawDescription = rawDescription;
        this.normalisedDescription = normalisedDescription;
        this.amount = amount;
        this.direction = direction;
        this.fingerprint = fingerprint;
        this.sourceFileHash = sourceFileHash;
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public LocalDate getBookedOn() {
        return bookedOn;
    }

    public String getRawDescription() {
        return rawDescription;
    }

    public String getNormalisedDescription() {
        return normalisedDescription;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Direction getDirection() {
        return direction;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public CategorisedBy getCategorisedBy() {
        return categorisedBy;
    }

    public void setCategorisedBy(CategorisedBy categorisedBy) {
        this.categorisedBy = categorisedBy;
    }

    public boolean isInternalTransfer() {
        return internalTransfer;
    }

    public void setInternalTransfer(boolean internalTransfer) {
        this.internalTransfer = internalTransfer;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getSourceFileHash() {
        return sourceFileHash;
    }

    public OffsetDateTime getIngestedAt() {
        return ingestedAt;
    }
}

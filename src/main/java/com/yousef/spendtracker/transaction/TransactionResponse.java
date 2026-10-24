package com.yousef.spendtracker.transaction;

import com.yousef.spendtracker.ingest.Direction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(Long id, LocalDate bookedOn, String rawDescription, String normalisedDescription,
                                   BigDecimal amount, Direction direction, Long categoryId,
                                   CategorisedBy categorisedBy, boolean internalTransfer) {

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getBookedOn(), t.getRawDescription(),
                t.getNormalisedDescription(), t.getAmount(), t.getDirection(), t.getCategoryId(),
                t.getCategorisedBy(), t.isInternalTransfer());
    }
}

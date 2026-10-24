package com.yousef.spendtracker.transfer;

import com.yousef.spendtracker.ingest.Direction;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The minimal shape {@link TransferMatcher} needs — decoupled from the JPA entity so it stays a pure function. */
public record TransferCandidate(long id, long accountId, LocalDate bookedOn, BigDecimal amount, Direction direction) {
}

package com.yousef.spendtracker.ingest;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One successfully parsed statement row. {@code amount} is always positive;
 * {@code direction} carries the sign.
 */
public record ParsedRow(LocalDate bookedOn, String rawDescription, BigDecimal amount, Direction direction) {
}

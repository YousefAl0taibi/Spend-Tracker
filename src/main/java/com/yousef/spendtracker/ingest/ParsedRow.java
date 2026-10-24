package com.yousef.spendtracker.ingest;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One successfully parsed statement row. {@code amount} is always positive;
 * {@code direction} carries the sign. {@code mccCode} is the card network's
 * merchant category code (e.g. "5411" for a grocery store) when the source
 * carried one — PDF card-transaction statements do, plain CSV exports don't,
 * so it is {@code null} there.
 */
public record ParsedRow(LocalDate bookedOn, String rawDescription, BigDecimal amount, Direction direction,
                         String mccCode) {

    public ParsedRow(LocalDate bookedOn, String rawDescription, BigDecimal amount, Direction direction) {
        this(bookedOn, rawDescription, amount, direction, null);
    }
}

package com.yousef.spendtracker.ingest;

import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * The idempotency key for a transaction. Re-uploading an overlapping statement
 * must produce the same fingerprint for the same real-world transaction, so the
 * database's unique constraint — not application code — is what prevents duplicates.
 *
 * Known limitation: two genuinely identical purchases on the same day, same
 * merchant, same amount collapse into one. Documented, not hidden.
 */
public final class Fingerprint {

    private Fingerprint() {
    }

    public static String of(long accountId, LocalDate bookedOn, BigDecimal amount,
                             Direction direction, String rawDescription) {
        String canonical = accountId + "|" + bookedOn + "|"
                + amount.setScale(2, RoundingMode.HALF_UP) + "|"
                + direction + "|"
                + rawDescription.trim().replaceAll("\\s+", " ");
        return DigestUtils.sha256Hex(canonical);
    }
}

package com.yousef.spendtracker.ingest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FingerprintTest {

    @Test
    void sameInputProducesSameHash() {
        String a = Fingerprint.of(1L, LocalDate.of(2026, 10, 24), new BigDecimal("125.50"),
                Direction.DEBIT, "TAMIMI MARKETS");
        String b = Fingerprint.of(1L, LocalDate.of(2026, 10, 24), new BigDecimal("125.50"),
                Direction.DEBIT, "TAMIMI MARKETS");

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSize(64);
    }

    @Test
    void whitespaceVariationInDescriptionProducesSameHash() {
        String a = Fingerprint.of(1L, LocalDate.of(2026, 10, 24), new BigDecimal("125.50"),
                Direction.DEBIT, "TAMIMI   MARKETS");
        String b = Fingerprint.of(1L, LocalDate.of(2026, 10, 24), new BigDecimal("125.50"),
                Direction.DEBIT, "  TAMIMI MARKETS  ");

        assertThat(a).isEqualTo(b);
    }

    @Test
    void differentAmountProducesDifferentHash() {
        String a = Fingerprint.of(1L, LocalDate.of(2026, 10, 24), new BigDecimal("125.50"),
                Direction.DEBIT, "TAMIMI MARKETS");
        String b = Fingerprint.of(1L, LocalDate.of(2026, 10, 24), new BigDecimal("125.51"),
                Direction.DEBIT, "TAMIMI MARKETS");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void differentAccountProducesDifferentHash() {
        String a = Fingerprint.of(1L, LocalDate.of(2026, 10, 24), new BigDecimal("125.50"),
                Direction.DEBIT, "TAMIMI MARKETS");
        String b = Fingerprint.of(2L, LocalDate.of(2026, 10, 24), new BigDecimal("125.50"),
                Direction.DEBIT, "TAMIMI MARKETS");

        assertThat(a).isNotEqualTo(b);
    }
}

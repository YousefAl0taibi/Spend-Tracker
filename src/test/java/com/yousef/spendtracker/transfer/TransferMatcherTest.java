package com.yousef.spendtracker.transfer;

import com.yousef.spendtracker.ingest.Direction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TransferMatcherTest {

    private final TransferMatcher matcher = new TransferMatcher();

    @Test
    void matchesDebitAgainstCreditOnDifferentAccountsWithinTwoDays() {
        TransferCandidate debit = new TransferCandidate(1, 1, LocalDate.of(2026, 10, 24),
                new BigDecimal("500.00"), Direction.DEBIT);
        TransferCandidate credit = new TransferCandidate(2, 2, LocalDate.of(2026, 10, 25),
                new BigDecimal("500.00"), Direction.CREDIT);

        Set<Long> matched = matcher.match(List.of(debit, credit));

        assertThat(matched).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void doesNotMatchTwoUnrelatedSameAmountSameDirectionTransactionsTwoDaysApart() {
        // The tricky case: same amount, two days apart, but BOTH debits — never a transfer pair.
        TransferCandidate first = new TransferCandidate(1, 1, LocalDate.of(2026, 10, 24),
                new BigDecimal("200.00"), Direction.DEBIT);
        TransferCandidate second = new TransferCandidate(2, 2, LocalDate.of(2026, 10, 26),
                new BigDecimal("200.00"), Direction.DEBIT);

        Set<Long> matched = matcher.match(List.of(first, second));

        assertThat(matched).isEmpty();
    }

    @Test
    void doesNotMatchWhenAccountsAreTheSame() {
        TransferCandidate debit = new TransferCandidate(1, 1, LocalDate.of(2026, 10, 24),
                new BigDecimal("300.00"), Direction.DEBIT);
        TransferCandidate credit = new TransferCandidate(2, 1, LocalDate.of(2026, 10, 24),
                new BigDecimal("300.00"), Direction.CREDIT);

        assertThat(matcher.match(List.of(debit, credit))).isEmpty();
    }

    @Test
    void doesNotMatchWhenMoreThanTwoDaysApart() {
        TransferCandidate debit = new TransferCandidate(1, 1, LocalDate.of(2026, 10, 24),
                new BigDecimal("300.00"), Direction.DEBIT);
        TransferCandidate credit = new TransferCandidate(2, 2, LocalDate.of(2026, 10, 28),
                new BigDecimal("300.00"), Direction.CREDIT);

        assertThat(matcher.match(List.of(debit, credit))).isEmpty();
    }

    @Test
    void alreadyMatchedTransactionIsNotMatchedAgain() {
        TransferCandidate debit = new TransferCandidate(1, 1, LocalDate.of(2026, 10, 24),
                new BigDecimal("300.00"), Direction.DEBIT);
        TransferCandidate credit1 = new TransferCandidate(2, 2, LocalDate.of(2026, 10, 24),
                new BigDecimal("300.00"), Direction.CREDIT);
        TransferCandidate credit2 = new TransferCandidate(3, 3, LocalDate.of(2026, 10, 24),
                new BigDecimal("300.00"), Direction.CREDIT);

        Set<Long> matched = matcher.match(List.of(debit, credit1, credit2));

        assertThat(matched).hasSize(2);
        assertThat(matched).contains(1L);
        assertThat(matched).containsAnyOf(2L, 3L);
    }
}

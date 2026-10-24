package com.yousef.spendtracker.transfer;

import com.yousef.spendtracker.ingest.Direction;

import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds pairs of transactions that are really money moved between the user's own
 * accounts, not spending: a debit on one account matched by a credit on another,
 * same absolute amount, booked within 2 days of each other.
 *
 * Deliberately a pure function over a plain candidate list, so it needs no database
 * to test — including the tricky negative case: two same-direction, same-amount
 * transactions close in time must never be matched to each other.
 */
public class TransferMatcher {

    private static final int MAX_DAYS_APART = 2;

    public Set<Long> match(List<TransferCandidate> candidates) {
        List<TransferCandidate> debits = candidates.stream()
                .filter(c -> c.direction() == Direction.DEBIT)
                .toList();
        List<TransferCandidate> credits = candidates.stream()
                .filter(c -> c.direction() == Direction.CREDIT)
                .toList();

        Set<Long> matched = new HashSet<>();

        for (TransferCandidate debit : debits) {
            if (matched.contains(debit.id())) {
                continue;
            }
            for (TransferCandidate credit : credits) {
                if (matched.contains(credit.id())) {
                    continue;
                }
                if (isTransferPair(debit, credit)) {
                    matched.add(debit.id());
                    matched.add(credit.id());
                    break;
                }
            }
        }

        return matched;
    }

    private boolean isTransferPair(TransferCandidate a, TransferCandidate b) {
        if (a.accountId() == b.accountId()) {
            return false;
        }
        if (a.amount().compareTo(b.amount()) != 0) {
            return false;
        }
        long daysApart = Math.abs(ChronoUnit.DAYS.between(a.bookedOn(), b.bookedOn()));
        return daysApart <= MAX_DAYS_APART;
    }
}

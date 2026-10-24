package com.yousef.spendtracker.transaction;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inserts one transaction, relying on the database's unique constraint on
 * {@code fingerprint} to reject duplicates — never a read-then-write check, which
 * races with itself under concurrent uploads.
 *
 * Each insert runs in its own transaction (REQUIRES_NEW): a rejected duplicate must
 * not abort the sibling rows being ingested in the same upload. This also keeps the
 * logic portable between the production PostgreSQL database and the in-memory H2
 * database used by the fast tests, instead of depending on a Postgres-only
 * {@code ON CONFLICT} clause.
 *
 * {@link #insertOrThrow} deliberately does not catch the constraint violation itself:
 * once Hibernate sees it at flush time it marks the transaction rollback-only, so the
 * exception must propagate out of this method (letting Spring complete the rollback of
 * this isolated transaction cleanly) before the caller — outside any transaction —
 * interprets it as "duplicate, not inserted".
 */
@Component
public class TransactionInserter {

    private final TransactionRepository transactionRepository;

    public TransactionInserter(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** @throws DataIntegrityViolationException if the fingerprint already exists. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertOrThrow(Transaction transaction) {
        transactionRepository.save(transaction);
        transactionRepository.flush();
    }
}

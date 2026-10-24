package com.yousef.spendtracker.ingest;

import com.yousef.spendtracker.categorise.CategoryGuess;
import com.yousef.spendtracker.categorise.RuleEngine;
import com.yousef.spendtracker.normalise.DescriptionNormaliser;
import com.yousef.spendtracker.transaction.Category;
import com.yousef.spendtracker.transaction.CategoryRepository;
import com.yousef.spendtracker.transaction.Transaction;
import com.yousef.spendtracker.transaction.TransactionInserter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The single place that turns "a date, a description, an amount and a direction" —
 * however they were obtained (a CSV row, a parsed SMS, anything later) — into a
 * normalised, categorised, de-duplicated {@link Transaction}. {@link IngestionService}
 * (file uploads) and {@code SmsIngestionService} (bank SMS) both funnel through this
 * one method, so there is exactly one place that decides what counts as "the same
 * transaction" and exactly one place that assigns a category.
 */
@Service
public class TransactionIngestor {

    private final DescriptionNormaliser normaliser;
    private final RuleEngine ruleEngine;
    private final CategoryRepository categoryRepository;
    private final TransactionInserter transactionInserter;

    public TransactionIngestor(DescriptionNormaliser normaliser, RuleEngine ruleEngine,
                                CategoryRepository categoryRepository, TransactionInserter transactionInserter) {
        this.normaliser = normaliser;
        this.ruleEngine = ruleEngine;
        this.categoryRepository = categoryRepository;
        this.transactionInserter = transactionInserter;
    }

    /** @return true if a new row was inserted, false if it was a duplicate. */
    public boolean ingestOne(long accountId, LocalDate bookedOn, String rawDescription, BigDecimal amount,
                              Direction direction, String sourceHash) {
        String normalisedDescription = normaliser.normalise(rawDescription);
        String fingerprint = Fingerprint.of(accountId, bookedOn, amount, direction, rawDescription);
        Long categoryId = guessCategoryId(normalisedDescription, direction);

        Transaction transaction = new Transaction(accountId, bookedOn, rawDescription, normalisedDescription,
                amount, direction, fingerprint, sourceHash);
        transaction.setCategoryId(categoryId);

        try {
            transactionInserter.insertOrThrow(transaction);
            return true;
        } catch (DataIntegrityViolationException duplicateFingerprint) {
            return false;
        }
    }

    private Long guessCategoryId(String normalisedDescription, Direction direction) {
        CategoryGuess guess = ruleEngine.categorise(normalisedDescription, direction);
        return categoryRepository.findByNameEn(guess.nameEn())
                .map(Category::getId)
                .orElse(null);
    }
}

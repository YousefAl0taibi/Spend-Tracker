package com.yousef.spendtracker.eval;

import com.yousef.spendtracker.categorise.CategoryGuess;
import com.yousef.spendtracker.categorise.RuleEngine;
import com.yousef.spendtracker.transaction.Category;
import com.yousef.spendtracker.transaction.CategorisedBy;
import com.yousef.spendtracker.transaction.CategoryRepository;
import com.yousef.spendtracker.transaction.Transaction;
import com.yousef.spendtracker.transaction.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hand-corrected transactions (categorised_by = MANUAL) are the evaluation set:
 * every correction is a labelled example, so no labelling effort is wasted.
 */
@Service
public class AccuracyReport {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final RuleEngine ruleEngine;

    public AccuracyReport(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
                           RuleEngine ruleEngine) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.ruleEngine = ruleEngine;
    }

    public AccuracyResponse ruleAccuracyOverManualCorrections() {
        List<Transaction> labelled = transactionRepository.findAll().stream()
                .filter(t -> t.getCategorisedBy() == CategorisedBy.MANUAL)
                .toList();

        Map<Long, String> categoryNamesById = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getNameEn));

        long correct = labelled.stream().filter(t -> isCorrectGuess(t, categoryNamesById)).count();
        int total = labelled.size();
        double accuracy = total == 0 ? 0.0 : (double) correct / total;

        return new AccuracyResponse(total, (int) correct, accuracy);
    }

    private boolean isCorrectGuess(Transaction transaction, Map<Long, String> categoryNamesById) {
        CategoryGuess guess = ruleEngine.categorise(transaction.getNormalisedDescription(), transaction.getDirection());
        String actualNameEn = categoryNamesById.get(transaction.getCategoryId());
        return guess.nameEn().equals(actualNameEn);
    }
}

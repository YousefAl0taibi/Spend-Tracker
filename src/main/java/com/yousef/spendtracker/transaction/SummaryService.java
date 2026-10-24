package com.yousef.spendtracker.transaction;

import com.yousef.spendtracker.ingest.Direction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public SummaryService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    /** Spending only (DEBIT rows). Internal transfers are excluded by default. */
    public MonthlySummaryResponse monthlySummary(int year, boolean includeTransfers) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = includeTransfers
                ? transactionRepository.findByBookedOnBetween(from, to)
                : transactionRepository.findByBookedOnBetweenAndInternalTransferFalse(from, to);

        Map<Long, String> categoryNamesById = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getNameEn));

        List<MonthSummary> months = transactions.stream()
                .filter(t -> t.getDirection() == Direction.DEBIT)
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getBookedOn())))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toMonthSummary(entry.getKey(), entry.getValue(), categoryNamesById))
                .toList();

        return new MonthlySummaryResponse(year, months);
    }

    private MonthSummary toMonthSummary(YearMonth month, List<Transaction> monthTransactions,
                                         Map<Long, String> categoryNamesById) {
        BigDecimal total = sum(monthTransactions);

        List<CategorySummary> byCategory = monthTransactions.stream()
                .collect(Collectors.groupingBy(t -> categoryNamesById.getOrDefault(t.getCategoryId(), "Uncategorised")))
                .entrySet().stream()
                .map(entry -> new CategorySummary(entry.getKey(), sum(entry.getValue()), entry.getValue().size()))
                .sorted(Comparator.comparing(CategorySummary::total).reversed())
                .toList();

        return new MonthSummary(month.toString(), total, byCategory);
    }

    private BigDecimal sum(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

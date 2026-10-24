package com.yousef.spendtracker.transaction;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public Page<TransactionResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDate effectiveFrom = from != null ? from : LocalDate.of(1970, 1, 1);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookedOn"));

        Page<Transaction> result = category != null
                ? transactionRepository.findByBookedOnBetweenAndCategoryId(effectiveFrom, effectiveTo, category, pageable)
                : transactionRepository.findByBookedOnBetween(effectiveFrom, effectiveTo, pageable);

        return result.map(TransactionResponse::from);
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@PathVariable Long id) {
        return TransactionResponse.from(findOrThrow(id));
    }

    @PatchMapping("/{id}/category")
    public TransactionResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
        Transaction transaction = findOrThrow(id);
        transaction.setCategoryId(request.categoryId());
        transaction.setCategorisedBy(CategorisedBy.MANUAL);
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    private Transaction findOrThrow(Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
    }
}

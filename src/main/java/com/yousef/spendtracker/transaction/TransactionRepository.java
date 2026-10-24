package com.yousef.spendtracker.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByBookedOnBetween(LocalDate from, LocalDate to, Pageable pageable);

    Page<Transaction> findByBookedOnBetweenAndCategoryId(LocalDate from, LocalDate to, Long categoryId, Pageable pageable);

    List<Transaction> findByBookedOnBetween(LocalDate from, LocalDate to);

    List<Transaction> findByBookedOnBetweenAndInternalTransferFalse(LocalDate from, LocalDate to);
}

package com.yousef.spendtracker.transaction;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Only for the {@code local} profile (H2 file database, no Flyway — see
 * application-local.yml). Mirrors V2__rules.sql's seed data so the categories
 * exist without needing Flyway to run against H2. Safe to restart: it checks
 * the table is empty first, so it never tries to insert duplicates.
 */
@Component
@Profile("local")
public class CategorySeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            return;
        }
        categoryRepository.save(new Category("Groceries", "بقالة"));
        categoryRepository.save(new Category("Fuel", "وقود"));
        categoryRepository.save(new Category("Dining", "مطاعم"));
        categoryRepository.save(new Category("Subscriptions", "اشتراكات"));
        categoryRepository.save(new Category("Salary", "راتب"));
        categoryRepository.save(new Category("Uncategorised", "غير مصنف"));
    }
}

package com.yousef.spendtracker.transaction;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Only for the {@code local} profile (H2 file database, no Flyway — see
 * application-local.yml). Mirrors the V2/V3 Flyway seed data so the categories
 * exist without needing Flyway to run against H2. Each category is checked by
 * name before inserting, so restarting after a new category was added here
 * (and the database already has the older ones) adds only what's missing.
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
        seedIfMissing("Groceries", "بقالة");
        seedIfMissing("Fuel", "وقود");
        seedIfMissing("Dining", "مطاعم");
        seedIfMissing("Subscriptions", "اشتراكات");
        seedIfMissing("Salary", "راتب");
        seedIfMissing("Uncategorised", "غير مصنف");
        seedIfMissing("Shopping", "تسوق");
        seedIfMissing("Health", "صحة");
        seedIfMissing("Transport", "تنقل");
        seedIfMissing("Automotive", "سيارات");
        seedIfMissing("Pets", "حيوانات");
        seedIfMissing("Entertainment", "ترفيه");
    }

    private void seedIfMissing(String nameEn, String nameAr) {
        if (categoryRepository.findByNameEn(nameEn).isEmpty()) {
            categoryRepository.save(new Category(nameEn, nameAr));
        }
    }
}

package com.yousef.spendtracker.categorise;

import com.yousef.spendtracker.ingest.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleEngineTest {

    private RuleEngine ruleEngine;

    @BeforeEach
    void loadRulesFromClasspath() {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("rules.yml")) {
            List<Rule> rules = RuleLoader.load(stream);
            ruleEngine = new RuleEngine(rules);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void matchesFirstPatternByPriorityOrder() {
        // Subscriptions (priority 5) must win over Dining (priority 20) if both matched,
        // but here only Subscriptions matches.
        CategoryGuess guess = ruleEngine.categorise("NETFLIX.COM", Direction.DEBIT);
        assertThat(guess.nameEn()).isEqualTo("Subscriptions");
    }

    @Test
    void groceriesAndFuelShareThePriorityTenButPatternsDoNotOverlap() {
        assertThat(ruleEngine.categorise("TAMIMI MARKETS", Direction.DEBIT).nameEn()).isEqualTo("Groceries");
        assertThat(ruleEngine.categorise("ALDREES PETROL STATION", Direction.DEBIT).nameEn()).isEqualTo("Fuel");
    }

    @Test
    void directionConditionIsRespected() {
        // SALARY pattern only applies to CREDIT; as a DEBIT it must fall through to Uncategorised.
        CategoryGuess credit = ruleEngine.categorise("SALARY OCT", Direction.CREDIT);
        assertThat(credit.nameEn()).isEqualTo("Salary");

        CategoryGuess debit = ruleEngine.categorise("SALARY OCT", Direction.DEBIT);
        assertThat(debit.nameEn()).isEqualTo("Uncategorised");
    }

    @Test
    void unmatchedDescriptionFallsBackToUncategorised() {
        CategoryGuess guess = ruleEngine.categorise("SOME RANDOM MERCHANT", Direction.DEBIT);
        assertThat(guess.nameEn()).isEqualTo("Uncategorised");
    }

    @Test
    void throwsIfNoCatchAllRuleExists() {
        Rule onlyGroceries = new Rule();
        onlyGroceries.setNameEn("Groceries");
        onlyGroceries.setNameAr("بقالة");
        onlyGroceries.setPriority(10);
        onlyGroceries.setPatterns(List.of("TAMIMI"));

        RuleEngine engineWithoutCatchAll = new RuleEngine(List.of(onlyGroceries));

        assertThatThrownBy(() -> engineWithoutCatchAll.categorise("UNKNOWN MERCHANT", Direction.DEBIT))
                .isInstanceOf(IllegalStateException.class);
    }
}

package com.yousef.spendtracker.categorise;

import com.yousef.spendtracker.ingest.Direction;

import java.util.Comparator;
import java.util.List;

/**
 * Matches a normalised description against {@code rules.yml}, lowest priority first,
 * first matching pattern wins. A pattern-less rule (the "Uncategorised" catch-all) always
 * matches, so this always returns a guess — never throws for unmatched input.
 */
public class RuleEngine {

    private final List<Rule> rulesByPriority;

    public RuleEngine(List<Rule> rules) {
        this.rulesByPriority = rules.stream()
                .sorted(Comparator.comparingInt(Rule::getPriority))
                .toList();
    }

    public CategoryGuess categorise(String normalisedDescription, Direction direction) {
        for (Rule rule : rulesByPriority) {
            if (rule.getDirection() != null && rule.getDirection() != direction) {
                continue;
            }
            if (rule.getPatterns().isEmpty()) {
                return new CategoryGuess(rule.getNameEn(), rule.getNameAr());
            }
            for (String pattern : rule.getPatterns()) {
                if (normalisedDescription.contains(pattern)) {
                    return new CategoryGuess(rule.getNameEn(), rule.getNameAr());
                }
            }
        }
        throw new IllegalStateException("No matching rule and no catch-all rule in rules.yml");
    }
}

package com.yousef.spendtracker.normalise;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Turns a raw bank statement description into a stable merchant string, e.g.
 * {@code "POS PURCHASE 4512*** TAMIMI MARKETS    RIYADH SA 24/10"} becomes
 * {@code "TAMIMI MARKETS"}.
 *
 * Each step is its own package-private static method so it can be unit-tested
 * in isolation, independently of the others.
 */
@Component
public class DescriptionNormaliser {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern MASKED_CARD = Pattern.compile("\\d{4}\\*+\\d*");
    private static final Pattern LONG_REFERENCE_DIGITS = Pattern.compile("\\b\\d{6,}\\b");
    private static final Pattern TRAILING_LOCATION_AND_DATE =
            Pattern.compile("\\s+[A-Z]{3,}(?:\\s+[A-Z]{2})?\\s+\\d{1,2}/\\d{1,2}\\s*$");
    private static final Pattern ARABIC_ALEF_VARIANTS = Pattern.compile("[آأإ]");
    private static final Pattern ARABIC_DIACRITICS = Pattern.compile("[ً-ْ]");

    private static final String[] KNOWN_PREFIXES = {
            "POS PURCHASE", "PURCHASE", "ATM WITHDRAWAL", "ATM", "TRANSFER TO", "TRANSFER",
            "PAYMENT TO", "PAYMENT"
    };

    public String normalise(String rawDescription) {
        String result = rawDescription;
        result = uppercaseAndCollapseWhitespace(result);
        result = stripMaskedNumbers(result);
        result = stripTrailingLocationAndDate(result);
        result = stripKnownPrefixes(result);
        result = normaliseArabic(result);
        return uppercaseAndCollapseWhitespace(result);
    }

    static String uppercaseAndCollapseWhitespace(String input) {
        if (input == null) {
            return "";
        }
        return WHITESPACE.matcher(input.trim()).replaceAll(" ").toUpperCase();
    }

    static String stripMaskedNumbers(String input) {
        String withoutMaskedCards = MASKED_CARD.matcher(input).replaceAll("");
        String withoutLongDigits = LONG_REFERENCE_DIGITS.matcher(withoutMaskedCards).replaceAll("");
        return WHITESPACE.matcher(withoutLongDigits.trim()).replaceAll(" ");
    }

    static String stripTrailingLocationAndDate(String input) {
        return TRAILING_LOCATION_AND_DATE.matcher(input).replaceAll("").trim();
    }

    static String stripKnownPrefixes(String input) {
        for (String prefix : KNOWN_PREFIXES) {
            if (input.startsWith(prefix)) {
                return input.substring(prefix.length()).trim();
            }
        }
        return input;
    }

    static String normaliseArabic(String input) {
        String result = ARABIC_ALEF_VARIANTS.matcher(input).replaceAll("ا"); // أ إ آ -> ا
        result = result.replace('ى', 'ي');                              // ى -> ي
        result = result.replace('ة', 'ه');                              // ة -> ه
        result = ARABIC_DIACRITICS.matcher(result).replaceAll("");                // diacritics
        return result.replace("ـ", "");                                      // tatweel
    }
}

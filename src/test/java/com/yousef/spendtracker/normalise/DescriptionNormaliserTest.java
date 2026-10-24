package com.yousef.spendtracker.normalise;

import org.junit.jupiter.api.Test;

import static com.yousef.spendtracker.normalise.DescriptionNormaliser.normaliseArabic;
import static com.yousef.spendtracker.normalise.DescriptionNormaliser.stripKnownPrefixes;
import static com.yousef.spendtracker.normalise.DescriptionNormaliser.stripMaskedNumbers;
import static com.yousef.spendtracker.normalise.DescriptionNormaliser.stripTrailingLocationAndDate;
import static com.yousef.spendtracker.normalise.DescriptionNormaliser.uppercaseAndCollapseWhitespace;
import static org.assertj.core.api.Assertions.assertThat;

class DescriptionNormaliserTest {

    private final DescriptionNormaliser normaliser = new DescriptionNormaliser();

    @Test
    void endToEndExampleFromSpec() {
        String raw = "POS PURCHASE 4512*** TAMIMI MARKETS    RIYADH SA 24/10";
        assertThat(normaliser.normalise(raw)).isEqualTo("TAMIMI MARKETS");
    }

    @Test
    void uppercasesAndCollapsesWhitespace() {
        assertThat(uppercaseAndCollapseWhitespace("  tamimi   markets  ")).isEqualTo("TAMIMI MARKETS");
    }

    @Test
    void stripsMaskedCardNumbers() {
        assertThat(stripMaskedNumbers("4512*** TAMIMI")).isEqualTo("TAMIMI");
    }

    @Test
    void stripsLongReferenceDigits() {
        assertThat(stripMaskedNumbers("HUNGERSTATION 778812 ORDER")).isEqualTo("HUNGERSTATION ORDER");
    }

    @Test
    void keepsShortDigitsThatAreNotReferenceNumbers() {
        assertThat(stripMaskedNumbers("STORE 24")).isEqualTo("STORE 24");
    }

    @Test
    void stripsTrailingLocationAndDate() {
        assertThat(stripTrailingLocationAndDate("TAMIMI MARKETS RIYADH SA 24/10"))
                .isEqualTo("TAMIMI MARKETS");
    }

    @Test
    void leavesDescriptionWithoutTrailingLocationUntouched() {
        assertThat(stripTrailingLocationAndDate("TAMIMI MARKETS")).isEqualTo("TAMIMI MARKETS");
    }

    @Test
    void stripsKnownPrefixes() {
        assertThat(stripKnownPrefixes("POS PURCHASE TAMIMI MARKETS")).isEqualTo("TAMIMI MARKETS");
        assertThat(stripKnownPrefixes("ATM WITHDRAWAL RIYAD BANK")).isEqualTo("RIYAD BANK");
        assertThat(stripKnownPrefixes("TRANSFER TO SAVINGS")).isEqualTo("SAVINGS");
    }

    @Test
    void leavesDescriptionWithoutKnownPrefixUntouched() {
        assertThat(stripKnownPrefixes("TAMIMI MARKETS")).isEqualTo("TAMIMI MARKETS");
    }

    @Test
    void normalisesArabicAlefVariants() {
        assertThat(normaliseArabic("أحمد إبراهيم آدم")).isEqualTo("احمد ابراهيم ادم");
    }

    @Test
    void normalisesArabicAlefMaksuraAndTaMarbuta() {
        assertThat(normaliseArabic("مقهى البركة")).isEqualTo("مقهي البركه");
    }

    @Test
    void stripsArabicDiacriticsAndTatweel() {
        assertThat(normaliseArabic("مَرحَـــباً")).isEqualTo("مرحبا");
    }

    @Test
    void handlesBilingualDescription() {
        String raw = "POS PURCHASE 4512*** مقهى البركة RIYADH SA 24/10";
        assertThat(normaliser.normalise(raw)).isEqualTo("مقهي البركه");
    }
}

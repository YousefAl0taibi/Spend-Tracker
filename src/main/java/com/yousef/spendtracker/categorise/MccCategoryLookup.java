package com.yousef.spendtracker.categorise;

import java.util.Map;
import java.util.Optional;

/**
 * Maps a card network's Merchant Category Code (MCC) — a 4-digit code every
 * mada/Visa/Mastercard POS transaction carries, e.g. "5411" for a grocery
 * store — to one of this app's category names.
 *
 * This is deliberately a free, offline, no-API alternative to looking a
 * merchant name up anywhere external: the code is already present in the
 * statement, assigned by the card network itself, so it needs no guessing and
 * leaks no merchant name to a third party. It only applies when the ingestion
 * source actually carries an MCC (a PDF card-transaction statement); CSV
 * exports don't, and fall back to {@link RuleEngine}'s merchant-name matching.
 *
 * Descriptions verified against
 * https://www.checkout.com/docs/resources/codes/merchant-category-codes
 * (the MCC standard itself is assigned by the card networks).
 */
public final class MccCategoryLookup {

    private static final Map<String, String> CATEGORY_BY_MCC = Map.ofEntries(
            // Groceries — grocery stores/supermarkets, bakeries, wholesale clubs
            Map.entry("5411", "Groceries"),
            Map.entry("5422", "Groceries"),
            Map.entry("5441", "Groceries"),
            Map.entry("5451", "Groceries"),
            Map.entry("5462", "Groceries"),
            Map.entry("5499", "Groceries"),
            Map.entry("5300", "Groceries"),
            Map.entry("5309", "Groceries"),

            // Fuel — service stations, automated fuel dispensers
            Map.entry("5541", "Fuel"),
            Map.entry("5542", "Fuel"),
            Map.entry("5172", "Fuel"),

            // Dining — restaurants, fast food, caterers, bars
            Map.entry("5811", "Dining"),
            Map.entry("5812", "Dining"),
            Map.entry("5813", "Dining"),
            Map.entry("5814", "Dining"),

            // Subscriptions — pay TV, telecom, digital goods
            Map.entry("4899", "Subscriptions"),
            Map.entry("4814", "Subscriptions"),
            Map.entry("5815", "Subscriptions"),
            Map.entry("5816", "Subscriptions"),
            Map.entry("5817", "Subscriptions"),
            Map.entry("5818", "Subscriptions"),

            // Shopping — clothing, electronics, general retail
            Map.entry("5611", "Shopping"),
            Map.entry("5621", "Shopping"),
            Map.entry("5631", "Shopping"),
            Map.entry("5641", "Shopping"),
            Map.entry("5651", "Shopping"),
            Map.entry("5655", "Shopping"),
            Map.entry("5661", "Shopping"),
            Map.entry("5691", "Shopping"),
            Map.entry("5699", "Shopping"),
            Map.entry("5732", "Shopping"),
            Map.entry("5733", "Shopping"),
            Map.entry("5734", "Shopping"),
            Map.entry("5941", "Shopping"),
            Map.entry("5942", "Shopping"),
            Map.entry("5944", "Shopping"),
            Map.entry("5945", "Shopping"),
            Map.entry("5310", "Shopping"),
            Map.entry("5311", "Shopping"),
            Map.entry("5331", "Shopping"),
            Map.entry("5399", "Shopping"),

            // Health — doctors, dentists, hospitals, pharmacies
            Map.entry("5912", "Health"),
            Map.entry("8011", "Health"),
            Map.entry("8021", "Health"),
            Map.entry("8031", "Health"),
            Map.entry("8041", "Health"),
            Map.entry("8042", "Health"),
            Map.entry("8043", "Health"),
            Map.entry("8049", "Health"),
            Map.entry("8050", "Health"),
            Map.entry("8062", "Health"),
            Map.entry("8071", "Health"),
            Map.entry("8099", "Health"),

            // Transport — taxis, buses, airlines, parking, car washes
            Map.entry("4111", "Transport"),
            Map.entry("4112", "Transport"),
            Map.entry("4121", "Transport"),
            Map.entry("4131", "Transport"),
            Map.entry("4511", "Transport"),
            Map.entry("7523", "Transport"),
            Map.entry("7542", "Transport"),

            // Automotive — dealers, parts, repair shops (non-dealer)
            Map.entry("5511", "Automotive"),
            Map.entry("5521", "Automotive"),
            Map.entry("5531", "Automotive"),
            Map.entry("5532", "Automotive"),
            Map.entry("5533", "Automotive"),
            Map.entry("7531", "Automotive"),
            Map.entry("7534", "Automotive"),
            Map.entry("7535", "Automotive"),
            Map.entry("7538", "Automotive"),

            // Pets
            Map.entry("5995", "Pets"),

            // Entertainment — cinemas, amusement parks, sports, zoos
            Map.entry("7832", "Entertainment"),
            Map.entry("7841", "Entertainment"),
            Map.entry("7911", "Entertainment"),
            Map.entry("7922", "Entertainment"),
            Map.entry("7929", "Entertainment"),
            Map.entry("7991", "Entertainment"),
            Map.entry("7992", "Entertainment"),
            Map.entry("7996", "Entertainment"),
            Map.entry("7997", "Entertainment"),
            Map.entry("7998", "Entertainment"),
            Map.entry("7999", "Entertainment")
    );

    private MccCategoryLookup() {
    }

    public static Optional<String> categoryNameFor(String mccCode) {
        return Optional.ofNullable(CATEGORY_BY_MCC.get(mccCode));
    }
}

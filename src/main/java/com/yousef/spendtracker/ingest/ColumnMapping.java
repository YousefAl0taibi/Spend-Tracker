package com.yousef.spendtracker.ingest;

/**
 * Describes how one bank's CSV export maps onto the fields {@link StatementParser}
 * needs. Either {debit, credit} or {amount} must be set, never both.
 */
public class ColumnMapping {

    private String date;
    private String description;
    private String debit;
    private String credit;
    private String amount;
    private String dateFormat;

    public ColumnMapping() {
    }

    public ColumnMapping(String date, String description, String debit, String credit,
                          String amount, String dateFormat) {
        this.date = date;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.amount = amount;
        this.dateFormat = dateFormat;
    }

    public static ColumnMapping debitCredit(String date, String description, String debit,
                                             String credit, String dateFormat) {
        return new ColumnMapping(date, description, debit, credit, null, dateFormat);
    }

    public static ColumnMapping signedAmount(String date, String description, String amount,
                                              String dateFormat) {
        return new ColumnMapping(date, description, null, null, amount, dateFormat);
    }

    public boolean usesSignedAmount() {
        return amount != null;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}

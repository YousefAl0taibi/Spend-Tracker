package com.yousef.spendtracker.sms;

import com.yousef.spendtracker.ingest.Direction;

/**
 * One recognisable bank SMS shape, configured as data (like {@code ColumnMapping} for
 * statement files) so a new bank's wording is a configuration change, not a code change.
 *
 * {@code pattern} is a regex with three named groups: {@code amount}, {@code merchant},
 * {@code date} and {@code time} (the last combined with {@code dateFormat} to produce
 * the booked date).
 */
public class SmsTemplate {

    private String name;
    private Direction direction;
    private String dateFormat;
    private String pattern;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}

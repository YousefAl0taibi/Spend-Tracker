package com.yousef.spendtracker.sms;

import com.yousef.spendtracker.ingest.Direction;

import java.math.BigDecimal;

public record SmsIngestionResponse(boolean recognised, boolean inserted, String merchant,
                                    BigDecimal amount, Direction direction) {

    public static SmsIngestionResponse unrecognised() {
        return new SmsIngestionResponse(false, false, null, null, null);
    }
}

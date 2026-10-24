package com.yousef.spendtracker.sms;

import jakarta.validation.constraints.NotBlank;

/** The body an on-device automation app (Tasker, MacroDroid, ...) posts for one received SMS. */
public record SmsIngestRequest(@NotBlank String text) {
}

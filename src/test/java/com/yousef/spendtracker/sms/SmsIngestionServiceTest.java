package com.yousef.spendtracker.sms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SmsIngestionServiceTest {

    private static final String SAMPLE_SMS = """
            شراء-POS

            بـ2.50 SAR

            من SHUMOOKH

            مدى-ابل*5012

            في 24/10/26 16:25""";

    @Autowired
    private SmsIngestionService smsIngestionService;

    @Test
    void recognisedSmsIsInsertedOnceAndIgnoredOnReplay() {
        SmsIngestionResponse first = smsIngestionService.ingest(SAMPLE_SMS);

        assertThat(first.recognised()).isTrue();
        assertThat(first.inserted()).isTrue();
        assertThat(first.merchant()).isEqualTo("SHUMOOKH");
        assertThat(first.amount()).isEqualByComparingTo(new BigDecimal("2.50"));

        // The exact same SMS arriving again (Tasker retried, or the automation double-fired).
        SmsIngestionResponse replay = smsIngestionService.ingest(SAMPLE_SMS);

        assertThat(replay.recognised()).isTrue();
        assertThat(replay.inserted()).isFalse();
    }

    @Test
    void unrecognisedTextIsReportedWithoutTouchingTheDatabase() {
        SmsIngestionResponse response = smsIngestionService.ingest("رمز التحقق 112233");

        assertThat(response.recognised()).isFalse();
        assertThat(response.inserted()).isFalse();
    }
}

package com.yousef.spendtracker.sms;

import com.yousef.spendtracker.ingest.Direction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SmsParserTest {

    private SmsParser madaPosParser() {
        SmsTemplate template = new SmsTemplate();
        template.setName("mada-pos-purchase");
        template.setDirection(Direction.DEBIT);
        template.setDateFormat("dd/MM/yy HH:mm");
        template.setPattern("شراء-POS\\s*بـ(?<amount>[0-9]+(?:\\.[0-9]+)?)\\s*(?<currency>[A-Za-z]+)\\s*"
                + "من\\s*(?<merchant>[^\\n]+?)\\s*مدى[^\\s]*\\s*في\\s*"
                + "(?<date>\\d{1,2}/\\d{1,2}/\\d{2,4})\\s+(?<time>\\d{1,2}:\\d{2})");

        SmsProperties properties = new SmsProperties();
        properties.setTemplates(List.of(template));
        return new SmsParser(properties);
    }

    @Test
    void parsesTheMadaPosPurchaseTemplate() {
        String sms = """
                شراء-POS

                بـ2.50 SAR

                من SHUMOOKH

                مدى-ابل*5012

                في 24/10/26 16:25""";

        Optional<ParsedSmsTransaction> parsed = madaPosParser().parse(sms);

        assertThat(parsed).isPresent();
        ParsedSmsTransaction transaction = parsed.get();
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("2.50"));
        assertThat(transaction.rawDescription()).isEqualTo("SHUMOOKH");
        assertThat(transaction.direction()).isEqualTo(Direction.DEBIT);
        assertThat(transaction.bookedOn()).isEqualTo(LocalDate.of(2026, 10, 24));
    }

    @Test
    void worksEvenWhenTheWholeMessageIsOneLine() {
        String sms = "شراء-POS بـ125.00 SAR من TAMIMI MARKETS مدى-ابل*5012 في 01/01/26 09:05";

        Optional<ParsedSmsTransaction> parsed = madaPosParser().parse(sms);

        assertThat(parsed).isPresent();
        assertThat(parsed.get().rawDescription()).isEqualTo("TAMIMI MARKETS");
        assertThat(parsed.get().amount()).isEqualByComparingTo(new BigDecimal("125.00"));
    }

    @Test
    void unrecognisedMessageReturnsEmptyRatherThanThrowing() {
        String otp = "رمز التحقق الخاص بك هو 483921، لا تشاركه مع أحد.";

        assertThat(madaPosParser().parse(otp)).isEmpty();
    }
}

package com.yousef.spendtracker.sms;

import com.yousef.spendtracker.ingest.Direction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches a raw bank SMS against the configured {@link SmsTemplate}s (first match wins)
 * and extracts a transaction from it. An SMS that matches none of them (an OTP, a
 * promotion, a bank from a template nobody has written yet) is simply not a transaction
 * as far as this app is concerned — {@link #parse} returns empty, never throws.
 */
@Component
public class SmsParser {

    private final List<CompiledTemplate> compiledTemplates;

    public SmsParser(SmsProperties properties) {
        this.compiledTemplates = properties.getTemplates().stream()
                .map(CompiledTemplate::compile)
                .toList();
    }

    public Optional<ParsedSmsTransaction> parse(String rawText) {
        for (CompiledTemplate template : compiledTemplates) {
            Matcher matcher = template.pattern().matcher(rawText);
            if (matcher.find()) {
                return Optional.of(template.toTransaction(matcher));
            }
        }
        return Optional.empty();
    }

    private record CompiledTemplate(Pattern pattern, Direction direction, DateTimeFormatter dateTimeFormatter) {

        static CompiledTemplate compile(SmsTemplate template) {
            Pattern pattern = Pattern.compile(template.getPattern(),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(template.getDateFormat());
            return new CompiledTemplate(pattern, template.getDirection(), formatter);
        }

        ParsedSmsTransaction toTransaction(Matcher matcher) {
            BigDecimal amount = new BigDecimal(matcher.group("amount").replace(",", ""));
            String merchant = matcher.group("merchant").trim();
            String dateTimeRaw = matcher.group("date") + " " + matcher.group("time");
            LocalDate bookedOn = LocalDateTime.parse(dateTimeRaw, dateTimeFormatter).toLocalDate();
            return new ParsedSmsTransaction(bookedOn, merchant, amount, direction);
        }
    }
}

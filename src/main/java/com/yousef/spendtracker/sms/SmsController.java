package com.yousef.spendtracker.sms;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Where an on-device automation app (Tasker, MacroDroid, ...) posts a bank SMS's raw
 * text the moment it arrives — no file, no cloud relay, just this device talking to
 * this app's own backend over the local network.
 */
@RestController
@RequestMapping("/sms")
public class SmsController {

    private final SmsIngestionService smsIngestionService;

    public SmsController(SmsIngestionService smsIngestionService) {
        this.smsIngestionService = smsIngestionService;
    }

    @PostMapping
    public SmsIngestionResponse ingest(@Valid @RequestBody SmsIngestRequest request) {
        return smsIngestionService.ingest(request.text());
    }
}

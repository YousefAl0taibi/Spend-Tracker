package com.yousef.spendtracker.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    private List<SmsTemplate> templates = new ArrayList<>();

    public List<SmsTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<SmsTemplate> templates) {
        this.templates = templates;
    }
}

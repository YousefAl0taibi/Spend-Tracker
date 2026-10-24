package com.yousef.spendtracker.categorise;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class RuleEngineConfig {

    @Bean
    public RuleEngine ruleEngine() throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("rules.yml")) {
            if (stream == null) {
                throw new IllegalStateException("rules.yml not found on the classpath");
            }
            List<Rule> rules = RuleLoader.load(stream);
            return new RuleEngine(rules);
        }
    }
}

package com.yousef.spendtracker.ingest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Binds {@code statement.mappings} from application.yml. Adding a new bank is a
 * configuration change, not a code change.
 */
@Component
@ConfigurationProperties(prefix = "statement")
public class StatementProperties {

    private Map<String, ColumnMapping> mappings = new HashMap<>();

    public Map<String, ColumnMapping> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, ColumnMapping> mappings) {
        this.mappings = mappings;
    }

    public ColumnMapping require(String name) {
        ColumnMapping mapping = mappings.get(name);
        if (mapping == null) {
            throw new IllegalArgumentException("Unknown statement mapping: " + name);
        }
        return mapping;
    }
}

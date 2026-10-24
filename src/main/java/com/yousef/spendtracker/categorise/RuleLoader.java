package com.yousef.spendtracker.categorise;

import com.yousef.spendtracker.ingest.Direction;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Reads rules.yml into {@link Rule}s. Deliberately independent of Spring's own
 *  property binding, so {@link RuleEngine} stays a plain, pure-function class. */
public final class RuleLoader {

    private RuleLoader() {
    }

    @SuppressWarnings("unchecked")
    public static List<Rule> load(InputStream yamlStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(yamlStream);
        List<Map<String, Object>> rawCategories = (List<Map<String, Object>>) root.get("categories");

        return rawCategories.stream().map(RuleLoader::toRule).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static Rule toRule(Map<String, Object> raw) {
        Rule rule = new Rule();
        rule.setNameEn((String) raw.get("name_en"));
        rule.setNameAr((String) raw.get("name_ar"));
        rule.setPriority(((Number) raw.getOrDefault("priority", 9999)).intValue());
        rule.setPatterns((List<String>) raw.getOrDefault("patterns", List.of()));

        String direction = (String) raw.get("direction");
        if (direction != null) {
            rule.setDirection(Direction.valueOf(direction));
        }
        return rule;
    }
}

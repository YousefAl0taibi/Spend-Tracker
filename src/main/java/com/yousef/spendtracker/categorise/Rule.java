package com.yousef.spendtracker.categorise;

import com.yousef.spendtracker.ingest.Direction;

import java.util.List;

/** One entry from rules.yml. Lower {@code priority} wins. */
public class Rule {

    private String nameEn;
    private String nameAr;
    private int priority = 9999;
    private List<String> patterns = List.of();
    private Direction direction;

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}

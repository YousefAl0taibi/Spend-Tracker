package com.yousef.spendtracker.eval;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eval")
public class EvalController {

    private final AccuracyReport accuracyReport;

    public EvalController(AccuracyReport accuracyReport) {
        this.accuracyReport = accuracyReport;
    }

    @GetMapping("/accuracy")
    public AccuracyResponse accuracy() {
        return accuracyReport.ruleAccuracyOverManualCorrections();
    }
}

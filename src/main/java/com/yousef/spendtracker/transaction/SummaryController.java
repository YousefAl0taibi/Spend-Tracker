package com.yousef.spendtracker.transaction;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/summary")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/monthly")
    public MonthlySummaryResponse monthly(@RequestParam int year,
                                           @RequestParam(defaultValue = "false") boolean includeTransfers) {
        return summaryService.monthlySummary(year, includeTransfers);
    }
}

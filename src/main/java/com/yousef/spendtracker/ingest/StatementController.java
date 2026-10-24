package com.yousef.spendtracker.ingest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/statements")
public class StatementController {

    private final StatementProperties statementProperties;
    private final IngestionService ingestionService;

    public StatementController(StatementProperties statementProperties, IngestionService ingestionService) {
        this.statementProperties = statementProperties;
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public IngestionResponse upload(@RequestParam("mapping") String mapping,
                                     @RequestParam("file") MultipartFile file) throws IOException {
        ColumnMapping columnMapping = statementProperties.require(mapping);
        return ingestionService.ingest(file, columnMapping);
    }
}

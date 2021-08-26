package com.github.collector.view.document;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document")
public class DocumentController {

    @PostMapping
    public DocumentDeduplicationResponse deduplicateDocuments(
            @RequestBody final DocumentDeduplicationRequest documentDeduplicationRequest) {

    }
}

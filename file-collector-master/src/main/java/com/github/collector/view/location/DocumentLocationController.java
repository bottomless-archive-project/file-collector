package com.github.collector.view.location;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document-location")
public class DocumentLocationController {

    @PostMapping
    public DeduplicateDocumentLocationResponse deduplicateDocumentLocations(
            @RequestBody final DeduplicateDocumentLocationRequest deduplicateDocumentLocationRequest) {

    }
}

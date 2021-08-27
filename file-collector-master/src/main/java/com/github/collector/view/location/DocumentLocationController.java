package com.github.collector.view.location;

import com.github.collector.service.location.DocumentLocationService;
import com.github.collector.view.location.request.DeduplicateDocumentLocationRequest;
import com.github.collector.view.location.response.DeduplicateDocumentLocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/document-location")
@RequiredArgsConstructor
public class DocumentLocationController {

    private final DocumentLocationService documentLocationService;

    @PostMapping
    public DeduplicateDocumentLocationResponse deduplicateDocumentLocations(
            @RequestBody final DeduplicateDocumentLocationRequest deduplicateDocumentLocationRequest) {
        final List<String> uniqueLocations = documentLocationService.deduplicateDocumentLocations(
                deduplicateDocumentLocationRequest.getLocations());

        return DeduplicateDocumentLocationResponse.builder()
                .locations(uniqueLocations)
                .build();
    }
}

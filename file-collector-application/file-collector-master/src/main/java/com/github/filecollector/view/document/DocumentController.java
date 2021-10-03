package com.github.filecollector.view.document;

import com.github.filecollector.service.document.DocumentService;
import com.github.filecollector.view.document.request.DocumentDeduplicationRequest;
import com.github.filecollector.view.document.response.DocumentDeduplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public DocumentDeduplicationResponse deduplicateDocuments(
            @RequestBody final DocumentDeduplicationRequest documentDeduplicationRequest) {
        final List<String> uniqueDocumentHashes = documentService.deduplicateDocuments(
                documentDeduplicationRequest.getHashes());

        return DocumentDeduplicationResponse.builder()
                .hashes(uniqueDocumentHashes)
                .build();
    }
}

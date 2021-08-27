package com.github.collector.service.document;

import com.github.collector.repository.document.DocumentRepository;
import com.github.collector.repository.document.domain.DocumentDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public List<String> deduplicateDocuments(final List<String> documentHashes) {
        return documentRepository.insertDocuments(
                        documentHashes.stream()
                                .map(documentHash -> {
                                    final DocumentDatabaseEntity documentDatabaseEntity = new DocumentDatabaseEntity();

                                    documentDatabaseEntity.setId(documentHash);

                                    return documentDatabaseEntity;
                                })
                                .toList()
                ).stream()
                .map(DocumentDatabaseEntity::getId)
                .toList();
    }
}

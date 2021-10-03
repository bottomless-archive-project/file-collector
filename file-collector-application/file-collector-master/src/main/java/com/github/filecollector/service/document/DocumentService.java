package com.github.filecollector.service.document;

import com.github.filecollector.repository.document.DocumentRepository;
import com.github.filecollector.repository.document.domain.DocumentDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public List<String> deduplicateDocuments(final Set<String> documentHashes) {
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

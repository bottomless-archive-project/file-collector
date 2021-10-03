package com.github.filecollector.location.service;

import com.github.filecollector.location.repository.DocumentLocationRepository;
import com.github.filecollector.location.repository.domain.DocumentLocationDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentLocationService {

    private final DocumentLocationRepository documentLocationRepository;

    public List<String> deduplicateDocumentLocations(final List<String> documentLocations) {
        return documentLocationRepository.insertDocuments(
                        documentLocations.stream()
                                .map(documentLocation -> {
                                    DocumentLocationDatabaseEntity documentLocationDatabaseEntity =
                                            new DocumentLocationDatabaseEntity();

                                    documentLocationDatabaseEntity.setId(DigestUtils.sha256Hex(documentLocation));
                                    documentLocationDatabaseEntity.setLocation(documentLocation);

                                    return documentLocationDatabaseEntity;
                                })
                                .toList()
                ).stream()
                .map(DocumentLocationDatabaseEntity::getLocation)
                .toList();
    }
}

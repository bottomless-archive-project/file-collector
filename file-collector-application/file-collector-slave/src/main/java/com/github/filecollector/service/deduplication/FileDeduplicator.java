package com.github.filecollector.service.deduplication;

import com.github.filecollector.service.domain.DeduplicationResult;
import com.github.filecollector.service.domain.TargetLocation;
import com.github.filecollector.service.hash.HashConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeduplicator {

    private final HashConverter hashConverter;
    private final FileDeduplicationClient fileDeduplicationClient;

    public List<DeduplicationResult> deduplicateFiles(final List<TargetLocation> downloadTargets) {
        log.info("Starting to deduplicate {} files.", downloadTargets.size());

        final Map<String, TargetLocation> hashPathMap = hashConverter.createHashesWithoutDuplicates(downloadTargets);

        return fileDeduplicationClient.deduplicateFiles(hashPathMap.keySet()).stream()
                .flatMap(uniqueHashes -> uniqueHashes.stream()
                        .map(hash -> {
                            final TargetLocation downloadTarget = hashPathMap.get(hash);

                            return DeduplicationResult.builder()
                                    .duplicate(!uniqueHashes.contains(hash))
                                    .fileLocation(downloadTarget)
                                    .hash(hash)
                                    .extension(downloadTarget.getExtension())
                                    .build();
                        })
                )
                .toList();
    }
}

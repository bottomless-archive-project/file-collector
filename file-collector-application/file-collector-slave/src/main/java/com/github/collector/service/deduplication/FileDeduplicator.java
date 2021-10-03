package com.github.collector.service.deduplication;

import com.github.collector.service.domain.DeduplicationResult;
import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.hash.HashConverter;
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

    public List<DeduplicationResult> deduplicateFiles(final List<DownloadTarget> downloadTargets) {
        log.info("Starting to deduplicate {} files.", downloadTargets.size());

        final Map<String, DownloadTarget> hashPathMap = hashConverter.createHashesWithoutDuplicates(downloadTargets);

        return fileDeduplicationClient.deduplicateFiles(hashPathMap.keySet()).stream()
                .flatMap(uniqueHashes -> uniqueHashes.stream()
                        .map(hash -> {
                            final DownloadTarget downloadTarget = hashPathMap.get(hash);

                            return DeduplicationResult.builder()
                                    .duplicate(!uniqueHashes.contains(hash))
                                    .fileLocation(downloadTarget.getTargetLocation())
                                    .hash(hash)
                                    .extension(downloadTarget.getSourceLocation().getExtension())
                                    .build();
                        })
                )
                .toList();
    }
}

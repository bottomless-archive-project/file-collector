package com.github.collector.service.deduplication;

import com.github.collector.service.domain.DeduplicationResult;
import com.github.collector.service.domain.DownloadTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeduplicator {

    private final FileDeduplicationClient fileDeduplicationClient;

    public Flux<DeduplicationResult> deduplicateFiles(final Map<String, DownloadTarget> hashPathMap) {
        log.info("Starting to deduplicate {} files.", hashPathMap.size());

        return fileDeduplicationClient.deduplicateFiles(hashPathMap.keySet())
                .flatMap(uniqueHashes -> Flux.fromIterable(uniqueHashes)
                        .map(hash -> {
                            final DownloadTarget downloadTarget = hashPathMap.get(hash);

                            return DeduplicationResult.builder()
                                    .duplicate(!uniqueHashes.contains(hash))
                                    .fileLocation(downloadTarget.getTargetLocation())
                                    .hash(hash)
                                    .extension(downloadTarget.getSourceLocation().getExtension())
                                    .build();
                        })
                );
    }
}

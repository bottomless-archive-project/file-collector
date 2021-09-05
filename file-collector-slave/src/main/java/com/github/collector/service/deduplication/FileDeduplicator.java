package com.github.collector.service.deduplication;

import com.github.collector.service.domain.DeduplicationResult;
import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.hash.HashConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeduplicator {

    private final HashConverter hashConverter;
    private final FileDeduplicationClient fileDeduplicationClient;

    public Flux<DeduplicationResult> deduplicateFiles(final List<DownloadTarget> downloadTargets) {
        log.info("Starting to deduplicate {} files.", downloadTargets.size());

        return Mono.fromCallable(() -> hashConverter.createHashesWithoutDuplicates(downloadTargets))
                .flatMapMany(hashPathMap -> fileDeduplicationClient.deduplicateFiles(hashPathMap.keySet())
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
                        )
                );
    }
}

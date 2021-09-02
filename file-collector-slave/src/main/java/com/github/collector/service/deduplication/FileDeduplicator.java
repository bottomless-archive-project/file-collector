package com.github.collector.service.deduplication;

import com.github.collector.service.domain.DeduplicationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeduplicator {

    private final FileDeduplicationClient fileDeduplicationClient;

    public List<DeduplicationResult> deduplicateFiles(final Map<String, Path> hashPathMap) {
        log.info("Starting to deduplicate {} files.", hashPathMap.size());

        List<String> uniqueHashes = fileDeduplicationClient.deduplicateFiles(hashPathMap.keySet());

        return hashPathMap.keySet().stream()
                .map(hash -> {
                    final Path fileLocation = hashPathMap.get(hash);
                    final String[] dotSplit = fileLocation.getFileName().toString().split("\\.");

                    return DeduplicationResult.builder()
                            .duplicate(!uniqueHashes.contains(hash))
                            .fileLocation(fileLocation)
                            .hash(hash)
                            .extension(dotSplit[dotSplit.length - 1])
                            .build();
                })
                .toList();
    }
}

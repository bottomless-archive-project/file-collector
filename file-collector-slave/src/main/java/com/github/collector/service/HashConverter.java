package com.github.collector.service;

import com.github.collector.service.domain.DownloadTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashConverter {

    private final Sha256ChecksumProvider sha256ChecksumProvider;

    public Map<String, DownloadTarget> calculateHashes(final List<DownloadTarget> downloadTargets) {
        final Map<String, DownloadTarget> hashPathMap = new HashMap<>();

        for (DownloadTarget downloadTarget : downloadTargets) {
            try {
                final String checksum = sha256ChecksumProvider.checksum(
                        downloadTarget.getTargetLocation().readAllBytes());

                if (hashPathMap.containsKey(checksum)) {
                    // Was in the batch already as a duplicate
                    downloadTarget.getTargetLocation().delete();
                } else {
                    hashPathMap.put(checksum, downloadTarget);
                }
            } catch (final IOException e) {
                log.error("Failed to calculate hash!", e);
            }
        }

        return hashPathMap;
    }
}

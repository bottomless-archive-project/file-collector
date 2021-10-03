package com.github.collector.service.hash;

import com.github.collector.service.domain.DownloadTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashConverter {

    public Map<String, DownloadTarget> createHashesWithoutDuplicates(final List<DownloadTarget> downloadTargets) {
        final Map<String, DownloadTarget> hashPathMap = new HashMap<>();

        for (DownloadTarget downloadTarget : downloadTargets) {
            try {
                final String checksum = calculateChecksum(downloadTarget);

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

    private String calculateChecksum(final DownloadTarget downloadTarget) throws IOException {
        return DigestUtils.sha256Hex(downloadTarget.getTargetLocation().inputStream());
    }
}

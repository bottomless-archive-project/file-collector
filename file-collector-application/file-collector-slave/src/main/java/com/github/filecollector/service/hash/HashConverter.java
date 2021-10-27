package com.github.filecollector.service.hash;

import com.github.filecollector.service.domain.TargetLocation;
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

    public Map<String, TargetLocation> createHashesWithoutDuplicates(final List<TargetLocation> targetLocations) {
        final Map<String, TargetLocation> hashPathMap = new HashMap<>();

        for (TargetLocation targetLocation : targetLocations) {
            try {
                final String checksum = calculateChecksum(targetLocation);

                if (hashPathMap.containsKey(checksum)) {
                    // Was in the batch already as a duplicate
                    targetLocation.delete();
                } else {
                    hashPathMap.put(checksum, targetLocation);
                }
            } catch (final IOException e) {
                log.error("Failed to calculate hash!", e);
            }
        }

        return hashPathMap;
    }

    private String calculateChecksum(final TargetLocation targetLocation) throws IOException {
        return DigestUtils.sha256Hex(targetLocation.inputStream());
    }
}

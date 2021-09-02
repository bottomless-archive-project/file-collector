package com.github.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HashConverter {

    private final Sha256ChecksumProvider sha256ChecksumProvider;

    public Map<String, Path> calculateHashes(final List<Path> paths) {
        final Map<String, Path> hashPathMap = new HashMap<>();

        for (Path path : paths) {
            try {
                final String checksum = sha256ChecksumProvider.checksum(Files.readAllBytes(path));

                if (hashPathMap.containsKey(checksum)) {
                    // Was in the batch already as a duplicate
                    Files.delete(path);
                } else {
                    hashPathMap.put(checksum, path);
                }
            } catch (IOException e) {
                // TODO: We need to handle this
                e.printStackTrace();
            }
        }

        return hashPathMap;
    }
}

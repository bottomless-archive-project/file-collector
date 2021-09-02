package com.github.collector.service.download;

import com.github.collector.configuration.FileConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Creates a location on a local resource (disc, network drive, etc.) where the downloaded file can be written.
 */
@Service
@RequiredArgsConstructor
public class TargetLocationFactory {

    private final FileConfigurationProperties fileConfigurationProperties;

    /**
     * Returns a location on a local resource (disc, network drive, etc.) where the downloaded file can be written.
     * The returned location is unique for every source location.
     */
    public Path newTargetLocation(final URL sourceLocation) {
        final String id = UUID.randomUUID().toString();

        final String extension = fileConfigurationProperties.getTypes().stream()
                .filter(suffix -> sourceLocation.getPath().endsWith(suffix))
                .findFirst()
                .orElse("");

        return Path.of(fileConfigurationProperties.getStageFolder() + "/" + id + "." + extension);
    }
}

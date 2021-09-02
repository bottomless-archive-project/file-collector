package com.github.collector.service.download;

import com.github.collector.configuration.FileConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class SourceLocationValidation {

    private final SourceLocationFactory sourceLocationFactory;
    private final FileConfigurationProperties fileCollectorProperties;

    public boolean shouldCrawlSource(final String rawSourceLocation) {
        return sourceLocationFactory.newSourceLocation(rawSourceLocation)
                .map(this::hasTargetExtension)
                .orElse(false);
    }

    private boolean hasTargetExtension(final URL sourceLocation) {
        return fileCollectorProperties.getTypes().stream()
                .anyMatch(type -> sourceLocation.getPath().endsWith("." + type));
    }
}

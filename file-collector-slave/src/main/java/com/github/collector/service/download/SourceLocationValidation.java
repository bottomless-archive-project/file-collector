package com.github.collector.service.download;

import com.github.collector.configuration.FileConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class SourceLocationValidation {

    private final FileConfigurationProperties fileCollectorProperties;

    public boolean shouldCrawlSource(final String rawSourceLocation) {
        if (rawSourceLocation.isEmpty()) {
            return false;
        }

        try {
            final URL sourceLocation = new URL(rawSourceLocation);

            return hasTargetExtension(sourceLocation);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private boolean hasTargetExtension(final URL sourceLocation) {
        return fileCollectorProperties.getTypes().stream()
                .anyMatch(type -> sourceLocation.getPath().endsWith("." + type));
    }
}

package com.github.filecollector.service.download;

import com.github.filecollector.configuration.FileConfigurationProperties;
import com.github.filecollector.service.domain.SourceLocation;
import com.github.filecollector.service.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TargetLocationFactory {

    private final FileConfigurationProperties fileConfigurationProperties;

    public TargetLocation newTargetLocation(final SourceLocation sourceLocation) {
        final String id = UUID.randomUUID().toString();

        final Path path = Path.of(fileConfigurationProperties.getStageFolder())
                .resolve(id + "." + sourceLocation.getExtension());

        return TargetLocation.builder()
                .path(path)
                .build();
    }
}

package com.github.filecollector.service.download;

import com.github.filecollector.service.domain.DownloadTarget;
import com.github.filecollector.service.domain.SourceLocation;
import com.github.filecollector.service.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DownloadTargetConverter {

    private final TargetLocationFactory targetLocationFactory;

    public Optional<DownloadTarget> convert(final String rawSourceLocation) {
        try {
            final SourceLocation sourceLocation = SourceLocation.builder()
                    .location(new URI(rawSourceLocation))
                    .build();

            final TargetLocation targetLocation = targetLocationFactory.newTargetLocation(sourceLocation);

            return Optional.of(
                    DownloadTarget.builder()
                            .sourceLocation(sourceLocation)
                            .targetLocation(targetLocation)
                            .build()
            );
        } catch (final URISyntaxException e) {
            return Optional.empty();
        }
    }
}

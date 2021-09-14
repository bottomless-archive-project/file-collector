package com.github.collector.service.download;

import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.domain.SourceLocation;
import com.github.collector.service.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
public class DownloadTargetConverter {

    private final TargetLocationFactory targetLocationFactory;

    public Mono<DownloadTarget> convert(final String rawSourceLocation) {
        try {
            final SourceLocation sourceLocation = SourceLocation.builder()
                    .location(new URI(rawSourceLocation))
                    .build();

            final TargetLocation targetLocation = targetLocationFactory.newTargetLocation(sourceLocation);

            return Mono.just(
                    DownloadTarget.builder()
                            .sourceLocation(sourceLocation)
                            .targetLocation(targetLocation)
                            .build()
            );
        } catch (final URISyntaxException e) {
            return Mono.empty();
        }
    }
}

package com.github.collector.service.download;

import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.domain.SourceLocation;
import com.github.collector.service.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class DownloadTargetConverter {

    private final TargetLocationFactory targetLocationFactory;

    public Mono<DownloadTarget> convert(final String rawSourceLocation) {
        return buildSourceLocation(rawSourceLocation)
                .map(sourceLocation -> {
                    final TargetLocation targetLocation = targetLocationFactory.newTargetLocation(sourceLocation);

                    return DownloadTarget.builder()
                            .sourceLocation(sourceLocation)
                            .targetLocation(targetLocation)
                            .build();
                });
    }

    private Mono<SourceLocation> buildSourceLocation(final String rawSourceLocation) {
        try {
            return Mono.just(
                    SourceLocation.builder()
                            .location(new URL(rawSourceLocation))
                            .build()
            );
        } catch (MalformedURLException e) {
            return Mono.empty();
        }
    }
}

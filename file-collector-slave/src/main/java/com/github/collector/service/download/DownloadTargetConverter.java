package com.github.collector.service.download;

import com.github.collector.service.domain.DownloadTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DownloadTargetConverter {

    private final TargetLocationFactory targetLocationFactory;

    public Mono<DownloadTarget> convert(final String sourceLocations) {
        try {
            final URL sourceLocation = new URL(sourceLocations);
            final Path targetLocation = targetLocationFactory.newTargetLocation(sourceLocation);

            return Mono.just(
                    DownloadTarget.builder()
                            .sourceLocation(sourceLocation)
                            .targetLocation(targetLocation)
                            .build()
            );
        } catch (MalformedURLException e) {
            return Mono.empty();
        }
    }
}

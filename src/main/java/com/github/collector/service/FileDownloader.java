package com.github.collector.service;

import com.github.collector.configuration.FileCollectorProperties;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloader {

    private final FileCollectorProperties fileCollectorProperties;
    private final WebClient downloaderWebClient;

    public Mono<Path> downloadFile(final String fileLocation) {
        final String id = UUID.randomUUID().toString();
        final String extension = fileCollectorProperties.getTypes().stream()
            .filter(fileLocation::endsWith)
            .findFirst()
            .orElse("");
        final Path targetLocation = Path.of("D:/downloader/" + id + "." + extension);

        return acquireFile(fileLocation, targetLocation)
            .onErrorReturn(targetLocation)
            .thenReturn(targetLocation);
    }

    private Mono<Path> acquireFile(final String downloadTarget, final Path resultLocation) {
        log.info("Downloading file: " + downloadTarget);

        try {
            final Flux<DataBuffer> dataBufferFlux = newDownloadRequest(downloadTarget);

            return DataBufferUtils.write(dataBufferFlux, resultLocation)
                .doOnError(error -> resultLocation.toFile().delete())
                .thenReturn(resultLocation);
        } catch (final Exception e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Mono.empty();
        }
    }

    public Flux<DataBuffer> newDownloadRequest(final String downloadTarget) {
        return downloaderWebClient.get()
            .uri(downloadTarget)
            .retrieve()
            .bodyToFlux(DataBuffer.class);
    }
}

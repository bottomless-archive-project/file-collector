package com.github.collector.service;

import com.github.collector.configuration.FileConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloader {

    private final FileConfigurationProperties fileConfigurationProperties;
    private final WebClient downloaderWebClient;
    private final AtomicLong downloadedFileCounter = new AtomicLong();

    public Optional<Path> downloadFile(final String fileLocation) {
        final String id = UUID.randomUUID().toString();
        final String extension = fileConfigurationProperties.getTypes().stream()
                .filter(fileLocation::endsWith)
                .findFirst()
                .orElse("");
        final Path targetLocation = Path.of("D:/downloader/" + id + "." + extension);

        return acquireFile(fileLocation, targetLocation);
    }

    private Optional<Path> acquireFile(final String downloadTarget, final Path resultLocation) {
        final long downloadedFileCount = downloadedFileCounter.incrementAndGet();

        if (downloadedFileCount % 100 == 0) {
            log.info("Downloaded {} files!", downloadedFileCount);
        }

        log.debug("Downloading file: " + downloadTarget);

        try {
            final Flux<DataBuffer> dataBufferFlux = newDownloadRequest(downloadTarget);

            return DataBufferUtils.write(dataBufferFlux, resultLocation)
                    .doOnError(error -> resultLocation.toFile().delete())
                    .thenReturn(resultLocation)
                    .map(Optional::of)
                    .block();
        } catch (final Exception e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Optional.empty();
        }
    }

    public Flux<DataBuffer> newDownloadRequest(final String downloadTarget) {
        return downloaderWebClient.get()
                .uri(downloadTarget)
                .retrieve()
                .bodyToFlux(DataBuffer.class);
    }
}

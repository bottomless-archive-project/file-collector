package com.github.collector.service;

import com.github.collector.configuration.FileConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloader {

    private final FileConfigurationProperties fileConfigurationProperties;
    private final AtomicLong downloadedFileCounter = new AtomicLong();

    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public Optional<Path> downloadFile(final String fileLocation) {
        log.info("Downloading file at {}.", fileLocation);

        final String id = UUID.randomUUID().toString();
        final String extension = fileConfigurationProperties.getTypes().stream()
                .filter(fileLocation::endsWith)
                .findFirst()
                .orElse("");
        final Path targetLocation = Path.of(fileConfigurationProperties.getStageFolder()
                + "/" + id + "." + extension);

        return acquireFile(fileLocation, targetLocation);
    }

    private Optional<Path> acquireFile(final String downloadTarget, final Path resultLocation) {
        final long downloadedFileCount = downloadedFileCounter.incrementAndGet();

        if (downloadedFileCount % 100 == 0) {
            log.info("Downloaded {} files!", downloadedFileCount);
        }

        log.debug("Downloading file: " + downloadTarget);

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(downloadTarget))
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();


            client.send(request, HttpResponse.BodyHandlers.ofFile(resultLocation));

            return Optional.of(resultLocation);
        } catch (final Exception e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Optional.empty();
        }
    }
}

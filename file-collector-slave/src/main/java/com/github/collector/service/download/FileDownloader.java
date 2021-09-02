package com.github.collector.service.download;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Downloads files from the internet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloader {

    private final HttpClient httpClient;

    /**
     * Download a file from the provided source location to the provided target location.
     *
     * @param sourceLocation the location to download the file from
     * @param targetLocation the location to save the file to
     * @return an empty optional if the download failed, the target location wrapped into an optional otherwise
     */
    public Optional<Path> downloadToFile(final URL sourceLocation, Path targetLocation) {
        log.debug("Downloading file at {}.", sourceLocation);

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(sourceLocation.toURI())
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofFile(targetLocation));

            return Optional.of(targetLocation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return Optional.empty();
        } catch (final IOException | URISyntaxException e) {
            log.debug("Failed to download document from location: {}.", sourceLocation, e);

            return Optional.empty();
        }
    }
}

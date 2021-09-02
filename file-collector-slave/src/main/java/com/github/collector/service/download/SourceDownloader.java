package com.github.collector.service.download;

import com.github.collector.service.domain.DownloadTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Downloads files from the internet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDownloader {

    private final HttpClient httpClient;

    /**
     * Download the provided download targets.
     *
     * @param downloadTargets the targets to download
     * @return the list to the successfully downloaded entries, failed entries are discarded and not in the result list
     */
    public List<Path> downloadToFile(final List<DownloadTarget> downloadTargets) {
        log.info("Starting to download {} urls.", downloadTargets.size());

        return downloadTargets.stream()
                .parallel()
                .map(this::downloadToFile)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<Path> downloadToFile(final DownloadTarget downloadTarget) {
        log.debug("Downloading file at {}.", downloadTarget.getSourceLocation());

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(downloadTarget.getSourceLocation().toURI())
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofFile(downloadTarget.getTargetLocation()));

            return Optional.of(downloadTarget.getTargetLocation());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return Optional.empty();
        } catch (final IOException | URISyntaxException e) {
            log.debug("Failed to download document from location: {}.", downloadTarget.getSourceLocation(), e);

            return Optional.empty();
        }
    }
}

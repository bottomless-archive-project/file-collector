package com.github.filecollector.service.download;

import com.github.filecollector.service.domain.DownloadTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDownloader {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public Optional<DownloadTarget> downloadToFile(final DownloadTarget downloadTarget) {
        log.info("Downloading: {}", downloadTarget.getSourceLocation().getLocation());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(downloadTarget.getSourceLocation().getLocation())
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofFile(
                    downloadTarget.getTargetLocation().getPath()));

            return Optional.of(downloadTarget);
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            log.debug("Download failed for source: {} with reason: {}.",
                    downloadTarget.getSourceLocation().getLocation(), e.getMessage());

            try {
                if (downloadTarget.getTargetLocation().exists()) {
                    downloadTarget.getTargetLocation().delete();
                }
            } catch (final IOException ex) {
                log.error("Failed to delete file on the staging location!", ex);
            }

            return Optional.empty();
        }
    }
}

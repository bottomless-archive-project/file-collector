package com.github.filecollector.service.download;

import com.github.filecollector.service.domain.SourceLocation;
import com.github.filecollector.service.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDownloader {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public Optional<TargetLocation> downloadToFile(final SourceLocation sourceLocation,
                                                   final TargetLocation targetLocation) {
        log.info("Downloading: {}", sourceLocation.getLocation());

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(sourceLocation.getLocation())
                .header("Accept-Encoding", "gzip")
                .build();

        try {
            final HttpResponse<InputStream> response = client.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            final String encoding = response.headers()
                    .firstValue("Content-Encoding")
                    .orElse("");

            if (encoding.equals("gzip")) {
                log.debug("File at {} is GZIP compressed!", sourceLocation.getLocation());

                try (InputStream is = new GZIPInputStream(response.body())) {
                    Files.copy(is, targetLocation.getPath());
                }
            } else {
                Files.copy(response.body(), targetLocation.getPath());
            }

            return Optional.of(targetLocation);
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            log.debug("Download failed for source: {} with reason: {}.",
                    sourceLocation.getLocation(), e.getMessage());

            try {
                if (targetLocation.exists()) {
                    targetLocation.delete();
                }
            } catch (final IOException ex) {
                log.error("Failed to delete file on the staging location!", ex);
            }

            return Optional.empty();
        }
    }
}

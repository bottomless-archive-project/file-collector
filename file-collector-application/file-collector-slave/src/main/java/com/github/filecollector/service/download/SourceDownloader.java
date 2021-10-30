package com.github.filecollector.service.download;

import com.github.filecollector.service.download.domain.SourceLocation;
import com.github.filecollector.service.download.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDownloader {

    private final HttpClient httpClient;

    public Optional<TargetLocation> downloadToFile(final SourceLocation sourceLocation,
                                                   final TargetLocation targetLocation) {
        log.debug("Downloading: {}", sourceLocation.getLocation());

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(sourceLocation.getLocation())
                    .header("Accept-Encoding", "gzip")
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofFile(targetLocation.getPath()));
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

        return Optional.of(targetLocation);
    }
}

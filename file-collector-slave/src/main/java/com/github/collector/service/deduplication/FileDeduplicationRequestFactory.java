package com.github.collector.service.deduplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.collector.configuration.MasterServerConfigurationProperties;
import com.github.collector.view.document.request.DocumentDeduplicationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Set;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class FileDeduplicationRequestFactory {

    private final ObjectMapper objectMapper;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    public HttpRequest newFileDeduplicationRequest(final Set<String> hashes) {
        try {
            final URI deduplicateDocumentLocations = URI.create(masterServerConfigurationProperties.getMasterLocation()
                    + "/document");

            final String requestBody = objectMapper.writeValueAsString(
                    DocumentDeduplicationRequest.builder()
                            .hashes(hashes)
                            .build()
            );

            return HttpRequest.newBuilder()
                    .uri(deduplicateDocumentLocations)
                    .timeout(Duration.of(10, SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Accept", "*/*")
                    .header("Content-Type", "application/json")
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize urls!", e);
        }
    }
}
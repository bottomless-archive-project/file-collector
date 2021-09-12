package com.github.collector.service.deduplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.collector.configuration.MasterServerConfigurationProperties;
import com.github.collector.view.location.request.DeduplicateDocumentLocationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class SourceDeduplicationRequestFactory {

    private final ObjectMapper objectMapper;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    public HttpRequest newSourceDeduplicationRequest(final List<String> urls) {
        try {
            final URI deduplicateDocumentLocations = URI.create(masterServerConfigurationProperties.getMasterLocation()
                    + "/document-location");

            final String requestBody = objectMapper.writeValueAsString(
                    DeduplicateDocumentLocationRequest.builder()
                            .locations(urls)
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
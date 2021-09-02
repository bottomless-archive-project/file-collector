package com.github.collector.service.deduplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.collector.view.location.response.DeduplicateDocumentLocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceLocationDeduplicationClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SourceDeduplicationRequestFactory sourceDeduplicationRequestFactory;

    public List<String> deduplicateSourceLocations(final List<String> sourceLocations) {
        try {
            log.info("Deduplication {} urls.", sourceLocations.size());

            final HttpRequest request = sourceDeduplicationRequestFactory.newSourceDeduplicationRequest(sourceLocations);

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            final DeduplicateDocumentLocationResponse deduplicateDocumentLocationResponse =
                    deserializeResponse(response);

            log.info("From the sent urls {} was unique.", deduplicateDocumentLocationResponse.getLocations().size());

            return deduplicateDocumentLocationResponse.getLocations();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return Collections.emptyList();
        } catch (IOException e) {
            //TODO: Add some retry logic!

            throw new IllegalStateException("Failed to do the deduplication!", e);
        }
    }

    private DeduplicateDocumentLocationResponse deserializeResponse(final HttpResponse<String> response)
            throws JsonProcessingException {
        return objectMapper.readValue(response.body(), DeduplicateDocumentLocationResponse.class);
    }
}

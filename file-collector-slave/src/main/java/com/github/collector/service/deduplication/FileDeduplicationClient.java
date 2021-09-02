package com.github.collector.service.deduplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.collector.view.document.response.DocumentDeduplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileDeduplicationClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final FileDeduplicationRequestFactory fileDeduplicationRequestFactory;

    public List<String> deduplicateFiles(final Set<String> hashes) {
        try {
            final HttpRequest request = fileDeduplicationRequestFactory.newFileDeduplicationRequest(hashes);

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            final DocumentDeduplicationResponse documentDeduplicationResponse =
                    objectMapper.readValue(response.body(), DocumentDeduplicationResponse.class);

            return documentDeduplicationResponse.getHashes();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return Collections.emptyList();
        } catch (IOException e) {
            //TODO: Add some retry logic!

            throw new IllegalStateException("Failed to do the file deduplication!", e);
        }
    }
}

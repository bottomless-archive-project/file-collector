package com.github.filecollector.service.deduplication;

import com.github.filecollector.configuration.MasterServerConfigurationProperties;
import com.github.filecollector.document.view.request.DocumentDeduplicationRequest;
import com.github.filecollector.document.view.response.DocumentDeduplicationResponse;
import com.github.mizosoft.methanol.BodyAdapter;
import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.TypeRef;
import com.github.mizosoft.methanol.adapter.jackson.JacksonAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeduplicationClient {

    private final HttpClient httpClient;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;
    private final BodyAdapter.Decoder decoder = JacksonAdapterFactory.createDecoder();
    private final BodyAdapter.Encoder encoder = JacksonAdapterFactory.createEncoder();

    @SneakyThrows
    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30000))
    public List<String> deduplicateFiles(final Set<String> hashes) {
        try {
            log.info("Deduplicating {} files.", hashes.size());

            final DocumentDeduplicationRequest documentDeduplicationRequest = DocumentDeduplicationRequest.builder()
                    .hashes(hashes)
                    .build();

            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(masterServerConfigurationProperties.getMasterLocation() + "/document"))
                    .POST(encoder.toBody(documentDeduplicationRequest, MediaType.APPLICATION_JSON))
                    .build();

            DocumentDeduplicationResponse documentDeduplicationResponse = httpClient.send(
                    httpRequest, info -> decoder.toObject(new TypeRef<DocumentDeduplicationResponse>() {
                    }, MediaType.APPLICATION_JSON)).body();

            log.info("From the sent {} file hashes {} was unique.", hashes.size(),
                    documentDeduplicationResponse.getHashes().size());

            return documentDeduplicationResponse.getHashes();
        } catch (Exception e) {
            log.error("Exception while doing the deduplication request!", e);

            throw e;
        }
    }
}

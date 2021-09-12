package com.github.collector.service.deduplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.collector.view.document.response.DocumentDeduplicationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeduplicationClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final FileDeduplicationRequestFactory fileDeduplicationRequestFactory;

    public Flux<List<String>> deduplicateFiles(final Set<String> hashes) {
        log.info("Deduplicating {} files.", hashes.size());

        return Mono.fromCallable(() -> {
                    try {
                        final HttpRequest request = fileDeduplicationRequestFactory.newFileDeduplicationRequest(hashes);

                        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        final DocumentDeduplicationResponse documentDeduplicationResponse =
                                objectMapper.readValue(response.body(), DocumentDeduplicationResponse.class);

                        return documentDeduplicationResponse.getHashes();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                        return Collections.<String>emptyList();
                    } catch (IOException e) {
                        //TODO: Add some retry logic!

                        throw new IllegalStateException("Failed to do the file deduplication!", e);
                    }
                })
                .flatMapMany(result -> {
                    log.info("From the sent {} file hashes {} was unique.", hashes.size(), result.size());

                    return Flux.just(result);
                });
    }
}

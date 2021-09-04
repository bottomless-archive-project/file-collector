package com.github.collector.service.deduplication;

import com.github.collector.configuration.MasterServerConfigurationProperties;
import com.github.collector.view.document.request.DocumentDeduplicationRequest;
import com.github.collector.view.document.response.DocumentDeduplicationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeduplicationClient {

    private final WebClient webClient;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    public Flux<List<String>> deduplicateFiles(final Set<String> hashes) {
        log.info("Deduplicating {} files.", hashes.size());

        final DocumentDeduplicationRequest documentDeduplicationRequest = DocumentDeduplicationRequest.builder()
                .hashes(hashes)
                .build();

        return webClient.post()
                .uri(URI.create(masterServerConfigurationProperties.getMasterLocation() + "/document"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(documentDeduplicationRequest)
                .retrieve()
                .bodyToFlux(DocumentDeduplicationResponse.class)
                .map(documentDeduplicationResponse -> {
                    log.info("From the sent {} file hashes {} was unique.", hashes.size(),
                            documentDeduplicationResponse.getHashes().size());

                    return documentDeduplicationResponse.getHashes();
                });
    }
}

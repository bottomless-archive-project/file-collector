package com.github.filecollector.service.deduplication;

import com.github.filecollector.configuration.MasterServerConfigurationProperties;
import com.github.filecollector.document.view.request.DocumentDeduplicationRequest;
import com.github.filecollector.document.view.response.DocumentDeduplicationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeduplicationClient {

    @Qualifier("masterWebClient")
    private final WebClient webClient;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    public List<String> deduplicateFiles(final Set<String> hashes) {
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
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(30)))
                .map(documentDeduplicationResponse -> {
                    log.info("From the sent {} file hashes {} was unique.", hashes.size(),
                            documentDeduplicationResponse.getHashes().size());

                    return documentDeduplicationResponse.getHashes();
                })
                .blockLast();
    }
}

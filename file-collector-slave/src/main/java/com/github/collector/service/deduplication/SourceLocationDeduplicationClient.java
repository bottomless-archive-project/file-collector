package com.github.collector.service.deduplication;

import com.github.collector.configuration.MasterServerConfigurationProperties;
import com.github.collector.view.location.request.DeduplicateDocumentLocationRequest;
import com.github.collector.view.location.response.DeduplicateDocumentLocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceLocationDeduplicationClient {

    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
            .build();
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    public Flux<String> deduplicateSourceLocations(final List<String> sourceLocations) {
        log.info("Deduplicating {} urls.", sourceLocations.size());

        final DeduplicateDocumentLocationRequest deduplicateDocumentLocationRequest =
                DeduplicateDocumentLocationRequest.builder()
                        .locations(sourceLocations)
                        .build();

        return webClient.post()
                .uri(URI.create(masterServerConfigurationProperties.getMasterLocation() + "/document-location"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deduplicateDocumentLocationRequest)
                .retrieve()
                .bodyToFlux(DeduplicateDocumentLocationResponse.class)
                .timeout(Duration.ofSeconds(30))
                //.retry()
                .flatMap(deduplicateDocumentLocationResponse -> {
                    log.info("From the sent urls {} only {} was unique.", sourceLocations.size(),
                            deduplicateDocumentLocationResponse.getLocations().size());

                    return Flux.fromIterable(deduplicateDocumentLocationRequest.getLocations());
                });
    }
}

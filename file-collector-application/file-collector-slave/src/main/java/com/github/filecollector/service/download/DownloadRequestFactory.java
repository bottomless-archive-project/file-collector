package com.github.filecollector.service.download;

import com.github.filecollector.service.domain.SourceLocation;
import com.github.filecollector.service.download.domain.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadRequestFactory {

    @Qualifier("downloaderWebClient")
    private final WebClient webClient;

    private final DownloadRequestRetryFactory downloadRequestRetryFactory;

    public Flux<DataBuffer> newDownloadRequest(final SourceLocation sourceLocation) {
        return webClient.get()
                .uri(sourceLocation.getLocation())
                .exchangeToFlux(clientResponse -> handleExchange(sourceLocation, clientResponse))
                .retryWhen(downloadRequestRetryFactory.newRetry());
    }

    private Flux<DataBuffer> handleExchange(final SourceLocation downloadTarget, final ClientResponse clientResponse) {
        if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            log.debug("Too many requests for location: {}. Retrying!", downloadTarget.getLocation());

            return Flux.error(new RetryableException());
        }

        return clientResponse.bodyToFlux(DataBuffer.class);
    }
}

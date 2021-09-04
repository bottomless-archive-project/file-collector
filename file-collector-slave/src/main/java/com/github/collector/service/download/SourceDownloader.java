package com.github.collector.service.download;

import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.download.domain.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDownloader {

    private final WebClient downloaderWebClient;

    public Mono<Path> downloadToFile(final DownloadTarget downloadTarget) {
        try {
            final Flux<DataBuffer> dataBufferFlux = newDownloadRequest(downloadTarget.getSourceLocation().toURI());

            return DataBufferUtils.write(dataBufferFlux, downloadTarget.getTargetLocation())
                    .doOnError(error -> downloadTarget.getTargetLocation().toFile().delete())
                    .thenReturn(downloadTarget.getTargetLocation())
                    .onErrorResume(error -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Error downloading a document: {}!", error.getMessage());
                        }

                        return Mono.empty();
                    });
        } catch (final URISyntaxException e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Mono.empty();
        }
    }

    private Flux<DataBuffer> newDownloadRequest(final URI downloadTarget) {
        return downloaderWebClient.get()
                .uri(downloadTarget)
                .exchangeToFlux(clientResponse -> handleExchange(downloadTarget, clientResponse))
                .retryWhen(newRetry());
    }

    private Flux<DataBuffer> handleExchange(final URI downloadTarget, final ClientResponse clientResponse) {
        if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            log.debug("Too many requests for location: {}. Retrying!", downloadTarget);

            return Flux.error(new RetryableException());
        }

        return clientResponse.bodyToFlux(DataBuffer.class);
    }

    /*
     * Create a retry that retries 3 times when the exception is a RetryableException. The initial backoff is 2 seconds
     * while the maximum backoff is 2 minutes.
     */
    private Retry newRetry() {
        return Retry.backoff(3, Duration.ofSeconds(5))
                .maxBackoff(Duration.ofMinutes(2))
                .filter(throwable -> {
                    if (shouldRetry(throwable)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Got exception when downloading: {}! Attempting to retry!",
                                    throwable.getClass().getName());
                        }

                        return true;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Got exception when downloading: {}!", throwable.getClass().getName());
                        }

                        return false;
                    }
                });
    }

    private boolean shouldRetry(final Throwable throwable) {
        return throwable instanceof RetryableException;
    }
}

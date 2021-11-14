package com.github.filecollector.service.download;

import com.github.filecollector.service.download.domain.SourceLocation;
import com.github.filecollector.service.download.domain.TargetLocation;
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

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDownloader {

    private final WebClient webClient;

    public Optional<TargetLocation> downloadToFile(final SourceLocation sourceLocation,
                                                   final TargetLocation targetLocation) {
        log.info("Downloading: {}", sourceLocation.getLocation());

        final Flux<DataBuffer> dataBufferFlux = newDownloadRequest(sourceLocation);

        return DataBufferUtils.write(dataBufferFlux, targetLocation.getPath())
                .doOnError(error -> {
                    try {
                        targetLocation.delete();
                    } catch (final IOException e) {
                        log.error("Failed to delete file on the staging location!", e);
                    }
                })
                .then(Mono.just(targetLocation))
                .onErrorResume(error -> {
                    if (log.isDebugEnabled()) {
                        log.info("Error downloading a document: {}!", error.getMessage());
                    }

                    return Mono.empty();
                })
            .blockOptional();
    }

    private Flux<DataBuffer> newDownloadRequest(final SourceLocation sourceLocation) {
        return webClient.get()
                .uri(sourceLocation.getLocation())
                .exchangeToFlux(clientResponse -> handleExchange(sourceLocation, clientResponse))
                .retryWhen(newRetry());
    }

    private Flux<DataBuffer> handleExchange(final SourceLocation downloadTarget, final ClientResponse clientResponse) {
        if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            log.info("Too many requests for location: {}. Retrying!", downloadTarget.getLocation());

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

    private static class RetryableException extends RuntimeException {

    }
}

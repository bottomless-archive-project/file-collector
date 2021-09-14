package com.github.collector.service.download;

import com.github.collector.service.domain.DownloadTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDownloader {

    private final DownloadRequestFactory downloadRequestFactory;

    public Optional<DownloadTarget> downloadToFile(final DownloadTarget downloadTarget) {
        final Flux<DataBuffer> dataBufferFlux = downloadRequestFactory.newDownloadRequest(downloadTarget.getSourceLocation());

        return DataBufferUtils.write(dataBufferFlux, downloadTarget.getTargetLocation().getPath())
                .doOnError(error -> {
                    try {
                        downloadTarget.getTargetLocation().delete();
                    } catch (final IOException e) {
                        log.error("Failed to delete file on the staging location!", e);
                    }
                })
                .then(Mono.just(downloadTarget))
                .onErrorResume(error -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Error downloading a document: {}!", error.getMessage());
                    }

                    return Mono.empty();
                })
                .blockOptional();
    }
}

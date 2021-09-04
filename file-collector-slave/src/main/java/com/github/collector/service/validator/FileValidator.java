package com.github.collector.service.validator;

import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.domain.TargetLocation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class FileValidator {

    public Mono<DownloadTarget> validateFiles(final DownloadTarget downloadTarget) {
        //TODO: add extension based validation

        final TargetLocation targetLocation = downloadTarget.getTargetLocation();

        try {
            if (targetLocation.exists() && !targetLocation.hasContent()) {
                targetLocation.delete();

                return Mono.empty();
            }
        } catch (final IOException e) {
            return Mono.empty();
        }

        return Mono.just(downloadTarget);
    }
}

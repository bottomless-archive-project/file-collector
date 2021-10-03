package com.github.filecollector.service.validator;

import com.github.filecollector.service.domain.DownloadTarget;
import com.github.filecollector.service.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadTargetValidator {

    private final List<Validator> validators;

    public Mono<DownloadTarget> validateFiles(final DownloadTarget downloadTarget) {
        final TargetLocation targetLocation = downloadTarget.getTargetLocation();

        try {
            if (targetLocation.exists() && !targetLocation.hasContent()) {
                log.info("Document at {} failed verification, removing it from the staging area.",
                        downloadTarget.getTargetLocation().getPath());

                targetLocation.delete();

                return Mono.empty();
            }

            final String extension = downloadTarget.getSourceLocation().getExtension();
            final boolean validationResult = validators.stream()
                    .filter(v -> v.isValidatorFor(extension))
                    .findFirst()
                    .map(v -> v.validate(targetLocation, extension))
                    .orElse(true);

            if (!validationResult) {
                targetLocation.delete();

                return Mono.empty();
            }
        } catch (final IOException e) {
            log.error("Failed to access document at {}.", downloadTarget.getTargetLocation().getPath(), e);

            return Mono.empty();
        }

        return Mono.just(downloadTarget);
    }
}

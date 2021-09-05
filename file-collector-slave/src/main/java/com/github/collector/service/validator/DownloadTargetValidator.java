package com.github.collector.service.validator;

import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DownloadTargetValidator {

    private final List<Validator> validators;

    public Mono<DownloadTarget> validateFiles(final DownloadTarget downloadTarget) {
        final TargetLocation targetLocation = downloadTarget.getTargetLocation();

        try {
            if (targetLocation.exists() && !targetLocation.hasContent()) {
                targetLocation.delete();

                return Mono.empty();
            }
        } catch (final IOException e) {
            return Mono.empty();
        }

        final String extension = downloadTarget.getSourceLocation().getExtension();
        final boolean validationResult = validators.stream()
                .filter(v -> v.isValidatorFor(extension))
                .findFirst()
                .map(v -> v.validate(targetLocation, extension))
                .orElse(true);

        if (!validationResult) {
            return Mono.empty();
        }

        return Mono.just(downloadTarget);
    }
}

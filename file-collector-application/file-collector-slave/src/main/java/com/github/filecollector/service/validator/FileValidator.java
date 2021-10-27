package com.github.filecollector.service.validator;

import com.github.filecollector.service.download.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileValidator {

    private final List<Validator> validators;

    public Optional<TargetLocation> validateFile(final TargetLocation targetLocation) {
        try {
            if (targetLocation.exists() && !targetLocation.hasContent()) {
                log.info("Document at {} failed verification, removing it from the staging area.",
                        targetLocation.getPath());

                targetLocation.delete();

                return Optional.empty();
            }

            final String extension = targetLocation.getExtension();
            final boolean validationResult = validators.stream()
                    .filter(v -> v.isValidatorFor(extension))
                    .findFirst()
                    .map(v -> v.validate(targetLocation, extension))
                    .orElse(true);

            if (!validationResult) {
                targetLocation.delete();

                return Optional.empty();
            }
        } catch (final IOException e) {
            log.error("Failed to access document at {}.", targetLocation.getPath(), e);

            return Optional.empty();
        }

        return Optional.of(targetLocation);
    }
}

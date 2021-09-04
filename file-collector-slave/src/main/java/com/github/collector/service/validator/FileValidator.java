package com.github.collector.service.validator;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileValidator {

    public Mono<Path> validateFiles(final Path downloadedFile) {
        //TODO: add extension based validation

        try {
            if (Files.exists(downloadedFile) && Files.size(downloadedFile) == 0) {
                Files.delete(downloadedFile);

                return Mono.empty();
            }
        } catch (final IOException e) {
            return Mono.empty();
        }

        return Mono.just(downloadedFile);
    }
}

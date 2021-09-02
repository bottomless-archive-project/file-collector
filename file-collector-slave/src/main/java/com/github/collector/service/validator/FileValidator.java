package com.github.collector.service.validator;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class FileValidator {

    public List<Path> validateFiles(final List<Path> files) {
        return files.stream()
                .filter(this::validateFile)
                .toList();
    }

    private boolean validateFile(final Path downloadedFile) {
        //TODO: add extension based validation

        try {
            if (Files.exists(downloadedFile) && Files.size(downloadedFile) == 0) {
                Files.delete(downloadedFile);

                return false;
            }
        } catch (final IOException e) {
            return false;
        }

        return true;
    }
}

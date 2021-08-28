package com.github.collector.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileValidator {

    public boolean validateFile(final Path downloadedFile) {
        try {
            if (Files.exists(downloadedFile) && Files.size(downloadedFile) == 0) {
                Files.delete(downloadedFile);

                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}

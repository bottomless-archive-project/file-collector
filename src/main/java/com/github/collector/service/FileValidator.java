package com.github.collector.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class FileValidator {

    public void validateFile(final Path downloadedFile) {
        try {
            if (Files.exists(downloadedFile) && Files.size(downloadedFile) == 0) {
                Files.delete(downloadedFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

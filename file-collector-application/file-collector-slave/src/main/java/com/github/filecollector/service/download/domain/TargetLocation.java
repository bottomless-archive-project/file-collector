package com.github.filecollector.service.download.domain;

import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Builder
public class TargetLocation {

    private final Path path;
    private final String extension;

    public boolean exists() {
        return Files.exists(path);
    }

    public boolean hasContent() throws IOException {
        return Files.size(path) != 0;
    }

    public void delete() throws IOException {
        Files.delete(path);
    }

    public InputStream inputStream() throws IOException {
        return Files.newInputStream(path);
    }

    public void move(final Path target) throws IOException {
        Files.move(path, target);
    }
}

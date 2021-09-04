package com.github.collector.service.domain;

import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Builder
public class TargetLocation {

    private final Path path;

    public boolean exists() {
        return Files.exists(path);
    }

    public boolean hasContent() throws IOException {
        return Files.size(path) != 0;
    }

    public void delete() throws IOException {
        Files.delete(path);
    }

    public byte[] readAllBytes() throws IOException {
        return Files.readAllBytes(path);
    }

    public void move(final Path target) throws IOException {
        Files.move(path, target);
    }
}

package com.github.collector.service.domain;

import lombok.Builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Builder
public class SourceLocation {

    private final URL location;

    public String getExtension() {
        final String[] dotSplit = location.getPath().split("\\.");

        return dotSplit[dotSplit.length - 1];
    }

    public URI toURI() throws URISyntaxException {
        return location.toURI();
    }
}

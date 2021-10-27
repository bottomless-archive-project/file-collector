package com.github.filecollector.service.download.domain;

import lombok.Builder;
import lombok.Getter;

import java.net.URI;

@Getter
@Builder
public class SourceLocation {

    private final URI location;

    public String getExtension() {
        final String[] dotSplit = location.getPath().split("\\.");

        return dotSplit[dotSplit.length - 1];
    }
}

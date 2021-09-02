package com.github.collector.service.domain;

import lombok.Builder;
import lombok.Value;

import java.net.URL;
import java.nio.file.Path;

@Value
@Builder
public class DownloadTarget {

    URL sourceLocation;
    Path targetLocation;
}

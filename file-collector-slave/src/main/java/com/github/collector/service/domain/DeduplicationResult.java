package com.github.collector.service.domain;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;

@Value
@Builder
public class DeduplicationResult {

    boolean duplicate;
    TargetLocation fileLocation;
    String hash;
    String extension;
}

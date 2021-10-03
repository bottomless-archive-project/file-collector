package com.github.collector.service.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DownloadTarget {

    SourceLocation sourceLocation;
    TargetLocation targetLocation;
}

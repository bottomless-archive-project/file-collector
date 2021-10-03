package com.github.filecollector.service.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DownloadTarget {

    SourceLocation sourceLocation;
    TargetLocation targetLocation;
}

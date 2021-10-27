package com.github.filecollector.service.domain;

import com.github.filecollector.service.download.domain.TargetLocation;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeduplicationResult {

    boolean duplicate;
    TargetLocation fileLocation;
    String hash;
    String extension;
}

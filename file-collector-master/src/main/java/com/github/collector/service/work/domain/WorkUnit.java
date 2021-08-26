package com.github.collector.service.work.domain;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class WorkUnit {

    UUID id;
    String location;
    WorkUnitStatus status;
}

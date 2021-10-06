package com.github.filecollector.workunit.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@ToString
public class WorkUnit {

    UUID id;
    List<String> locations;
}

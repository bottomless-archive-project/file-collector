package com.github.collector.view.work.response;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class StartWorkUnitResponse {

    UUID id;
    String location;
}

package com.github.collector.view.work.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Value
@Builder
@Jacksonized
public class StartWorkUnitResponse {

    UUID id;
    String location;
}

package com.github.filecollector.view.work.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@Jacksonized
public class StartWorkUnitResponse {

    UUID id;
    List<String> locations;
}

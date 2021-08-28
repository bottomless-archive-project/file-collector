package com.github.collector.view.work.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class FinishWorkUnitRequest {

    String workUnitId;
}

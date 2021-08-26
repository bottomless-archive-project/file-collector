package com.github.collector.view.work.request;

import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
public class FinishWorkUnitRequest {

    String workUnitId;
}

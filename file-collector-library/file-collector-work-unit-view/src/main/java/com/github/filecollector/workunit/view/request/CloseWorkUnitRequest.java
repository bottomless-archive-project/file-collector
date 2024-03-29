package com.github.filecollector.workunit.view.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CloseWorkUnitRequest {

    String workUnitId;
}

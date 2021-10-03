package com.github.filecollector.view.location.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class DeduplicateDocumentLocationRequest {

    List<String> locations;
}

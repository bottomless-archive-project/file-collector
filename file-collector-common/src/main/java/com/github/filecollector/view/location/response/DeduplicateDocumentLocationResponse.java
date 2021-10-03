package com.github.filecollector.view.location.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class DeduplicateDocumentLocationResponse {

    List<String> locations;
}

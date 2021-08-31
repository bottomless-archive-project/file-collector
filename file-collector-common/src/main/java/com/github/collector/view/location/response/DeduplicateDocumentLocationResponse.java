package com.github.collector.view.location.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DeduplicateDocumentLocationResponse {

    List<String> locations;
}

package com.github.collector.view.document.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class DocumentDeduplicationRequest {

    List<String> hashes;
}

package com.github.filecollector.view.document.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

@Value
@Builder
@Jacksonized
public class DocumentDeduplicationRequest {

    Set<String> hashes;
}

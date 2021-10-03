package com.github.filecollector.view.document.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class DocumentDeduplicationResponse {

    List<String> hashes;
}

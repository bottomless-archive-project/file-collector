package com.github.collector.view.document.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DocumentDeduplicationResponse {

    List<String> hashes;
}

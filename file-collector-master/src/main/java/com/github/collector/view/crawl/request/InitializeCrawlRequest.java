package com.github.collector.view.crawl.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class InitializeCrawlRequest {

    String crawlId;
}

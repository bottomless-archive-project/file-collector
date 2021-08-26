package com.github.collector.service.work.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParsingContext {

    private final String baseUrl;
    private final String content;
}

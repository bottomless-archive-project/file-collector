package com.github.collector.service.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParsingContext {

    private final String baseUrl;
    private final String content;
}

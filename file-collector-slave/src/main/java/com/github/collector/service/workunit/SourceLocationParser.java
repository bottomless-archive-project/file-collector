package com.github.collector.service.workunit;

import com.github.collector.service.domain.ParsingContext;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class SourceLocationParser {

    public Flux<String> parseLocations(final ParsingContext parsingContext) {
        return parseDocument(parsingContext.getBaseUrl(), parsingContext.getContent())
                .flatMapMany(document -> Flux.fromIterable(document.select("a"))
                        .map(element -> element.absUrl("href"))
                );
    }

    private Mono<Document> parseDocument(final String warcRecordUrl, final String contentString) {
        try {
            return Mono.fromCallable(() -> Jsoup.parse(contentString, warcRecordUrl));
        } catch (final Exception e) {
            log.error("Failed to parse document.", e);

            return Mono.empty();
        }
    }
}

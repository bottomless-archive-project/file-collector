package com.github.collector.service.workunit;

import com.github.collector.service.domain.ParsingContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SourceLocationParser {

    public Flux<String> parseLocations(final ParsingContext parsingContext) {
        return parseDocument(parsingContext.getBaseUrl(), parsingContext.getContent())
                .flatMapMany(document -> Flux.fromStream(document.select("a").stream())
                        .map(element -> element.absUrl("href"))
                        .filter(url -> !url.isEmpty())
                );
    }

    private Mono<Document> parseDocument(final String warcRecordUrl, final String contentString) {
        try {
            return Mono.fromCallable(() -> Jsoup.parse(contentString, warcRecordUrl));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}

package com.github.collector.service;

import com.github.collector.service.domain.ParsingContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FileLocationParser {

    public Flux<String> parseLocations(final ParsingContext parsingContext) {
        return Mono.fromSupplier(() -> parseDocument(parsingContext.getBaseUrl(), parsingContext.getContent()))
            .flatMapIterable(document -> document.select("a"))
            .map(element -> element.absUrl("href"))
            .filter(url -> !url.isEmpty());
    }

    private Document parseDocument(final String warcRecordUrl, final String contentString) {
        try {
            return Jsoup.parse(contentString, warcRecordUrl);
        } catch (Exception e) {
            return null;
        }
    }
}

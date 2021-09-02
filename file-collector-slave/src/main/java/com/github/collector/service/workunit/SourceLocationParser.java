package com.github.collector.service.workunit;

import com.github.collector.service.domain.ParsingContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SourceLocationParser {

    public List<String> parseLocations(final ParsingContext parsingContext) {
        return parseDocument(parsingContext.getBaseUrl(), parsingContext.getContent())
                .map(document -> document
                        .select("a").stream()
                        .map(element -> element.absUrl("href"))
                        .filter(url -> !url.isEmpty())
                        .toList()
                )
                .orElse(Collections.emptyList());
    }

    private Optional<Document> parseDocument(final String warcRecordUrl, final String contentString) {
        try {
            return Optional.of(Jsoup.parse(contentString, warcRecordUrl));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

package com.github.filecollector.workunit;

import com.github.filecollector.service.domain.ParsingContext;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class SourceLocationParser {

    public Stream<String> parseLocations(final ParsingContext parsingContext) {
        return parseDocument(parsingContext.getBaseUrl(), parsingContext.getContent()).stream()
                .flatMap(document -> document.select("a").stream()
                        .map(element -> element.absUrl("href"))
                );
    }

    private Optional<Document> parseDocument(final String warcRecordUrl, final String contentString) {
        try {
            return Optional.of(Jsoup.parse(contentString, warcRecordUrl));
        } catch (final Exception e) {
            log.error("Failed to parse document.", e);

            return Optional.empty();
        }
    }
}

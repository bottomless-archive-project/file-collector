package com.github.collector.service;

import com.github.collector.service.domain.ParsingContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileLocationParser {

    public List<String> parseLocations(final ParsingContext parsingContext) {
        return parseDocument(parsingContext.getBaseUrl(), parsingContext.getContent())
                .select("a").stream()
                .map(element -> element.absUrl("href"))
                .filter(url -> !url.isEmpty())
                .toList();
    }

    private Document parseDocument(final String warcRecordUrl, final String contentString) {
        try {
            return Jsoup.parse(contentString, warcRecordUrl);
        } catch (Exception e) {
            return null; //TODO: why????
        }
    }
}

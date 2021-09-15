package com.github.collector.service.workunit;

import com.github.bottomlessarchive.warc.service.WarcFormatException;
import com.github.bottomlessarchive.warc.service.WarcReader;
import com.github.bottomlessarchive.warc.service.content.domain.WarcContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import com.github.collector.service.download.SourceLocationValidation;
import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitParser {

    private final SourceLocationParser sourceLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final SourceLocationValidation sourceLocationValidation;

    public List<String> parseSourceLocations(final WorkUnit workUnit) {
        try {
            final WarcReader warcReader = new WarcReader(new URL(workUnit.getLocation()));
            final Set<String> urlsInWorkUnit = new HashSet<>();

            Optional<WarcRecord<WarcContentBlock>> optionalWarcRecord;
            do {
                optionalWarcRecord = readNextRecord(warcReader);

                urlsInWorkUnit.addAll(
                        optionalWarcRecord
                                .map(this::parseWarcRecord)
                                .orElse(Collections.emptySet())
                );
            } while (optionalWarcRecord.isPresent());

            return new ArrayList<>(urlsInWorkUnit);
        } catch (final Exception e) {
            log.error("Failed to crawl urls!", e);

            return Collections.emptyList();
        }
    }

    private Optional<WarcRecord<WarcContentBlock>> readNextRecord(final WarcReader warcReader) {
        try {
            return warcReader.readRecord();
        } catch (final WarcFormatException e) {
            log.debug("Unable to parse warc file: " + e.getMessage());

            return readNextRecord(warcReader);
        }
    }

    private Set<String> parseWarcRecord(final WarcRecord<WarcContentBlock> warcRecord) {
        return Stream.of(warcRecord)
                .filter(WarcRecord::isResponse)
                .map(parsingContextFactory::buildParsingContext)
                .flatMap(sourceLocationParser::parseLocations)
                .filter(sourceLocationValidation::shouldCrawlSource)
                .collect(Collectors.toSet());
    }
}

package com.github.collector.service.workunit;

import com.github.bottomlessarchive.warc.service.WarcFormatException;
import com.github.bottomlessarchive.warc.service.WarcParsingException;
import com.github.bottomlessarchive.warc.service.WarcReader;
import com.github.bottomlessarchive.warc.service.content.domain.WarcContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.domain.SourceLocation;
import com.github.collector.service.download.SourceDownloader;
import com.github.collector.service.download.SourceLocationValidation;
import com.github.collector.service.download.TargetLocationFactory;
import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitParser {

    private final SourceDownloader sourceDownloader;
    private final TargetLocationFactory targetLocationFactory;
    private final SourceLocationParser sourceLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final SourceLocationValidation sourceLocationValidation;

    public List<String> parseSourceLocations(final WorkUnit workUnit) {
        try {
            // Download to local file
            log.info("Started downloading the crawl file.");
            final SourceLocation warcSourceLocation = SourceLocation.builder()
                    .location(new URI(workUnit.getLocation()))
                    .build();
            final DownloadTarget downloadTarget = DownloadTarget.builder()
                    .sourceLocation(warcSourceLocation)
                    .targetLocation(targetLocationFactory.newTargetLocation(warcSourceLocation))
                    .build();

            sourceDownloader.downloadToFile(downloadTarget)
                    .block();

            final WarcReader warcReader = new WarcReader(downloadTarget.getTargetLocation().inputStream());
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

            downloadTarget.getTargetLocation().delete();

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
        try {
            return Stream.of(warcRecord)
                    .filter(WarcRecord::isResponse)
                    .map(parsingContextFactory::buildParsingContext)
                    .flatMap(sourceLocationParser::parseLocations)
                    .filter(sourceLocationValidation::shouldCrawlSource)
                    .collect(Collectors.toSet());
        } catch (final WarcParsingException e) {
            log.debug("Unable to parse warc file: " + e.getMessage());

            return Collections.emptySet();
        }
    }
}

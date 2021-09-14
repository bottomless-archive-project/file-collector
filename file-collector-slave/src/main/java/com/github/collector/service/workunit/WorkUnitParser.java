package com.github.collector.service.workunit;

import com.github.bottomlessarchive.warc.service.WarcRecordStreamFactory;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecordType;
import com.github.collector.service.download.SourceLocationValidation;
import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitParser {

    private final SourceLocationParser sourceLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final SourceLocationValidation sourceLocationValidation;

    public List<String> parseSourceLocations(final WorkUnit workUnit) {
        return WarcRecordStreamFactory.streamOf(workUnit.getLocation(), WarcRecordType.RESPONSE)
                .map(parsingContextFactory::buildParsingContext)
                .flatMap(sourceLocationParser::parseLocations)
                .filter(sourceLocationValidation::shouldCrawlSource)
                .toList();
    }
}

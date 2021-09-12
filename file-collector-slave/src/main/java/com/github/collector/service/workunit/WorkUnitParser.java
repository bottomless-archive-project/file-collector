package com.github.collector.service.workunit;

import com.github.bottomlessarchive.warc.service.WarcRecordFluxFactory;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecordType;
import com.github.collector.service.download.SourceLocationValidation;
import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class WorkUnitParser {

    private final SourceLocationParser sourceLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final SourceLocationValidation sourceLocationValidation;

    public Flux<String> parseSourceLocations(final WorkUnit workUnit) {
        return WarcRecordFluxFactory.buildWarcRecordFlux(workUnit.getLocation(), WarcRecordType.RESPONSE)
                .map(parsingContextFactory::buildParsingContext)
                .flatMap(sourceLocationParser::parseLocations)
                .filter(sourceLocationValidation::shouldCrawlSource);
    }
}

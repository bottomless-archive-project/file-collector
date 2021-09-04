package com.github.collector.service.workunit;

import com.github.collector.service.download.SourceLocationValidation;
import com.github.collector.service.warc.WarcFluxFactory;
import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class WorkUnitParser {

    private final WarcFluxFactory warcFluxFactory;
    private final SourceLocationParser sourceLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final SourceLocationValidation sourceLocationValidation;

    public Flux<String> parseSourceLocations(final WorkUnit workUnit) {
        try {
            return warcFluxFactory.buildWarcRecordFlux(new URL(workUnit.getLocation()))
                    .map(parsingContextFactory::buildParsingContext)
                    .flatMap(sourceLocationParser::parseLocations)
                    .filter(sourceLocationValidation::shouldCrawlSource);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to parse work unit!", e);
        }
    }
}

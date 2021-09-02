package com.github.collector.service.workunit;

import com.github.bottomlessarchive.warc.service.WarcRecordStreamFactory;
import com.github.bottomlessarchive.warc.service.content.response.domain.ResponseContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecordType;
import com.github.collector.service.download.SourceLocationValidation;
import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkUnitParser {

    private final SourceLocationParser sourceLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final SourceLocationValidation sourceLocationValidation;

    public Set<String> parseSourceLocations(final WorkUnit workUnit) {
        try {
            return WarcRecordStreamFactory.<ResponseContentBlock>streamOf(
                            new URL(workUnit.getLocation()), List.of(WarcRecordType.RESPONSE))
                    .map(parsingContextFactory::buildParsingContext)
                    .flatMap(parsingContext -> sourceLocationParser.parseLocations(parsingContext).stream())
                    .filter(sourceLocationValidation::shouldCrawlSource)
                    .collect(Collectors.toSet());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to parse work unit!", e);
        }
    }
}

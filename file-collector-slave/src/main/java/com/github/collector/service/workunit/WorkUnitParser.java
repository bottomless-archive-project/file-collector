package com.github.collector.service.workunit;

import com.github.bottomlessarchive.warc.service.WarcFormatException;
import com.github.bottomlessarchive.warc.service.WarcRecordIteratorFactory;
import com.github.bottomlessarchive.warc.service.content.domain.WarcContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import com.github.collector.service.domain.ParsingContext;
import com.github.collector.service.download.SourceLocationValidation;
import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitParser {

    private final SourceLocationParser sourceLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final SourceLocationValidation sourceLocationValidation;

    public List<String> parseSourceLocations(final WorkUnit workUnit) {
        try {
            final Iterator<WarcRecord<WarcContentBlock>> warcRecordIterator =
                    WarcRecordIteratorFactory.iteratorOf(workUnit.getLocation());

            final ArrayList<String> resultUrlList = new ArrayList<>();

            while (true) {
                try {
                    if (!warcRecordIterator.hasNext()) {
                        break;
                    }

                    final WarcRecord<WarcContentBlock> warcRecord = warcRecordIterator.next();

                    // Skipping everything not a response
                    if (!warcRecord.isResponse()) {
                        continue;
                    }

                    final ParsingContext parsingContext = parsingContextFactory.buildParsingContext(warcRecord);

                    final List<String> urls = sourceLocationParser.parseLocations(parsingContext)
                            .filter(sourceLocationValidation::shouldCrawlSource)
                            .toList();

                    resultUrlList.addAll(urls);
                } catch (WarcFormatException e) {
                    log.error("Unable to parse warc file: " + e.getMessage());
                }
            }

            return resultUrlList;
        } catch (final Exception e) {
            log.error("Failed to crawl urls!", e);

            return Collections.emptyList();
        }
    }
}

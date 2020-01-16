package com.github.collector.command;

import com.github.bottomlessarchive.commoncrawl.WarcLocationFactory;
import com.github.bottomlessarchive.warc.service.WarcRecordStreamFactory;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import com.github.collector.configuration.FileCollectorProperties;
import com.github.collector.service.FileDownloader;
import com.github.collector.service.FileLocationParser;
import com.github.collector.service.FileValidator;
import com.github.collector.service.ParsingContextFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    private final WarcLocationFactory warcLocationFactory;
    private final FileCollectorProperties fileCollectorProperties;
    private final ParsingContextFactory parsingContextFactory;
    private final FileLocationParser fileLocationParser;
    private final FileDownloader fileDownloader;
    private final FileValidator fileValidator;

    @Override
    public void run(final String... args) {
        if (fileCollectorProperties.getTypes() == null) {
            throw new RuntimeException("No file types are selected for collection!");
        }

        Flux.fromIterable(warcLocationFactory.newUrls(fileCollectorProperties.getCrawlId()))
            .flatMap(insideWarcLocation -> Flux.fromStream(() -> WarcRecordStreamFactory.streamOf(insideWarcLocation)))
            .filter(WarcRecord::isResponse)
            .map(parsingContextFactory::buildParsingContext)
            .parallel()
            .flatMap(fileLocationParser::parseLocations)
            .runOn(Schedulers.boundedElastic())
            .filter(fileLocation -> fileCollectorProperties.getTypes().stream()
                .anyMatch(fileLocation::endsWith)
            )
            .flatMap(fileDownloader::downloadFile)
            .doOnNext(fileValidator::validateFile)
            .subscribe();
    }
}

package com.github.collector.command;

import com.github.collector.service.deduplication.FileDeduplicator;
import com.github.collector.service.deduplication.SourceLocationDeduplicationClient;
import com.github.collector.service.download.DownloadTargetConverter;
import com.github.collector.service.download.DownloadTargetFinalizer;
import com.github.collector.service.download.SourceDownloader;
import com.github.collector.service.validator.DownloadTargetValidator;
import com.github.collector.service.workunit.WorkUnitGenerator;
import com.github.collector.service.workunit.WorkUnitManipulator;
import com.github.collector.service.workunit.WorkUnitParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDownloaderCommand implements CommandLineRunner {

    private final WorkUnitGenerator workUnitGenerator;
    private final WorkUnitParser workUnitParser;
    private final SourceDownloader sourceDownloader;
    private final DownloadTargetValidator downloadTargetValidator;
    private final DownloadTargetConverter downloadTargetConverter;
    private final SourceLocationDeduplicationClient sourceLocationDeduplicationClient;
    private final WorkUnitManipulator workUnitManipulator;
    private final FileDeduplicator fileDeduplicator;
    private final DownloadTargetFinalizer downloadTargetFinalizer;

    @Override
    public void run(String... args) {
        Hooks.onOperatorDebug();

        Flux.generate(workUnitGenerator)
                .flatMap(workUnit -> Mono.just(workUnit)
                        .flatMapMany(workUnitParser::parseSourceLocations)
                        .buffer(100)
                        .flatMap(sourceLocationDeduplicationClient::deduplicateSourceLocations)
                        .map(downloadTargetConverter::convert)
                        .flatMap(sourceDownloader::downloadToFile)
                        .flatMap(downloadTargetValidator::validateFiles)
                        .buffer(100)
                        .flatMap(fileDeduplicator::deduplicateFiles)
                        .flatMap(downloadTargetFinalizer::finalizeDownloadTargets)
                        .doOnError(error -> log.error("Failed to catch an error on the work unit level!", error))
                        .then(Mono.just(workUnit))
                )
                .doOnNext(workUnitManipulator::closeWorkUnit)
                .doOnError(error -> log.error("Failed to catch an error on the command level!", error))
                .subscribe();
    }
}

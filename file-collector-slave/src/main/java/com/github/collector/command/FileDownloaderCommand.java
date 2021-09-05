package com.github.collector.command;

import com.github.collector.service.deduplication.FileDeduplicator;
import com.github.collector.service.deduplication.SourceLocationDeduplicationClient;
import com.github.collector.service.download.DownloadTargetConverter;
import com.github.collector.service.download.DownloadTargetFinalizer;
import com.github.collector.service.download.SourceDownloader;
import com.github.collector.service.validator.DownloadTargetValidator;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.service.workunit.WorkUnitManipulator;
import com.github.collector.service.workunit.WorkUnitParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDownloaderCommand implements CommandLineRunner {

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
        Flux.generate((Consumer<SynchronousSink<WorkUnit>>) synchronousSink -> {
                    final WorkUnit workUnit = workUnitManipulator.startWorkUnit();

                    synchronousSink.next(workUnit);
                })
                .flatMap(workUnit -> Mono.just(workUnit)
                        .flatMapMany(workUnitParser::parseSourceLocations)
                        .buffer(100)
                        .flatMap(sourceLocationDeduplicationClient::deduplicateSourceLocations)
                        .flatMap(downloadTargetConverter::convert)
                        .flatMap(sourceDownloader::downloadToFile)
                        .flatMap(downloadTargetValidator::validateFiles)
                        .buffer(100)
                        .flatMap(fileDeduplicator::deduplicateFiles)
                        .doOnNext(downloadTargetFinalizer::finalizeDownloadTargets)
                        .then(Mono.just(workUnit))
                )
                .doOnNext(workUnitManipulator::closeWorkUnit)
                .subscribe();
    }
}

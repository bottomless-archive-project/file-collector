package com.github.collector.command;

import com.github.collector.service.deduplication.FileDeduplicator;
import com.github.collector.service.deduplication.SourceLocationDeduplicationClient;
import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.download.DownloadTargetConverter;
import com.github.collector.service.download.DownloadTargetFinalizer;
import com.github.collector.service.download.SourceDownloader;
import com.github.collector.service.validator.DownloadTargetValidator;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.service.workunit.WorkUnitGenerator;
import com.github.collector.service.workunit.WorkUnitManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDownloaderCommand implements CommandLineRunner {

    private final WorkUnitGenerator workUnitGenerator;
    private final SourceDownloader sourceDownloader;
    private final DownloadTargetValidator downloadTargetValidator;
    private final DownloadTargetConverter downloadTargetConverter;
    private final SourceLocationDeduplicationClient sourceLocationDeduplicationClient;
    private final WorkUnitManipulator workUnitManipulator;
    private final FileDeduplicator fileDeduplicator;
    private final DownloadTargetFinalizer downloadTargetFinalizer;

    @Override
    public void run(String... args) {
        while (true) {
            final WorkUnit workUnit = workUnitManipulator.startWorkUnit();

            log.info("Started processing work unit: {}.", workUnit.getId());

            final Set<DownloadTarget> downloadTargets = Stream.of(workUnit.getLocations())
                    .map(sourceLocationDeduplicationClient::deduplicateSourceLocations) //TODO: The dedup should happen on the master before returning the work unit
                    .flatMap(rawSourceLocation -> rawSourceLocation.stream()
                            .map(downloadTargetConverter::convert))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());

            final List<DownloadTarget> resultFiles = Flux.fromIterable(downloadTargets)
                    .flatMap(sourceDownloader::downloadToFile)
                    .flatMap(downloadTargetValidator::validateFiles)
                    .buffer()
                    .blockLast();

            log.info("Got {} successfully downloaded documents.", resultFiles.size());

            Stream.of(resultFiles)
                    .flatMap(deduplicate -> fileDeduplicator.deduplicateFiles(deduplicate).stream())
                    .forEach(downloadTargetFinalizer::finalizeDownloadTargets);

            log.info("Finished work unit: {}.", workUnit);

            workUnitManipulator.closeWorkUnit(workUnit);
        }
    }
}

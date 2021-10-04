package com.github.filecollector.command;

import com.github.filecollector.service.deduplication.FileDeduplicator;
import com.github.filecollector.service.domain.DownloadTarget;
import com.github.filecollector.service.download.DownloadTargetConverter;
import com.github.filecollector.service.download.DownloadTargetFinalizer;
import com.github.filecollector.service.download.SourceDownloader;
import com.github.filecollector.service.validator.DownloadTargetValidator;
import com.github.filecollector.workunit.service.domain.WorkUnit;
import com.github.filecollector.workunit.WorkUnitManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDownloaderCommand implements CommandLineRunner {

    private final SourceDownloader sourceDownloader;
    private final DownloadTargetValidator downloadTargetValidator;
    private final DownloadTargetConverter downloadTargetConverter;
    private final WorkUnitManipulator workUnitManipulator;
    private final FileDeduplicator fileDeduplicator;
    private final DownloadTargetFinalizer downloadTargetFinalizer;

    @Override
    public void run(String... args) {
        while (true) {
            final WorkUnit workUnit = workUnitManipulator.startWorkUnit();

            log.info("Started processing work unit: {}.", workUnit.getId());

            final List<DownloadTarget> resultFiles = Flux.fromIterable(workUnit.getLocations())
                    .map(downloadTargetConverter::convert)
                    .flatMap(Mono::justOrEmpty)
                    .flatMap(sourceDownloader::downloadToFile)
                    .flatMap(downloadTargetValidator::validateFiles)
                    .toStream()
                    .toList();

            log.info("Got {} successfully downloaded documents.", resultFiles.size());

            Stream.of(resultFiles)
                    .flatMap(deduplicate -> fileDeduplicator.deduplicateFiles(deduplicate).stream())
                    .forEach(downloadTargetFinalizer::finalizeDownloadTargets);

            log.info("Finished work unit: {}.", workUnit);

            workUnitManipulator.closeWorkUnit(workUnit);
        }
    }
}

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
import com.github.collector.service.workunit.WorkUnitParser;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

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
        while (true) {
            final WorkUnit workUnit = workUnitManipulator.startWorkUnit();

            log.info("Started processing work unit: {}.", workUnit.getLocation());

            final List<String> urls = workUnitParser.parseSourceLocations(workUnit);

            log.info("Parsed {} urls.", urls.size());

            final List<DownloadTarget> resultFiles = Lists.partition(urls, 100).stream()
                    .parallel()
                    .flatMap(sourceLocations -> sourceLocationDeduplicationClient
                            .deduplicateSourceLocations(sourceLocations).stream())
                    .flatMap(rawSourceLocation -> downloadTargetConverter.convert(rawSourceLocation).stream())
                    .flatMap(downloadTarget -> sourceDownloader.downloadToFile(downloadTarget).stream())
                    .flatMap(downloadTarget -> downloadTargetValidator.validateFiles(downloadTarget).stream())
                    .toList();

            log.info("Got {} successfully downloaded documents.", resultFiles.size());

            Lists.partition(resultFiles, 100).stream()
                    .parallel()
                    .flatMap(deduplicate -> fileDeduplicator.deduplicateFiles(deduplicate).stream())
                    .forEach(downloadTargetFinalizer::finalizeDownloadTargets);

            log.info("Finished work unit: {}.", workUnit);

            workUnitManipulator.closeWorkUnit(workUnit);
        }
    }
}

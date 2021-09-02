package com.github.collector.command;

import com.github.collector.configuration.FileConfigurationProperties;
import com.github.collector.service.HashConverter;
import com.github.collector.service.deduplication.FileDeduplicator;
import com.github.collector.service.deduplication.SourceLocationDeduplicationClient;
import com.github.collector.service.domain.DeduplicationResult;
import com.github.collector.service.download.DownloadTargetConverter;
import com.github.collector.service.download.SourceDownloader;
import com.github.collector.service.validator.FileValidator;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.service.workunit.WorkUnitParser;
import com.github.collector.service.workunit.WorkUnitProvider;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDownloaderCommand implements CommandLineRunner {

    private final HashConverter hashConverter;
    private final WorkUnitParser workUnitParser;
    private final SourceDownloader sourceDownloader;
    private final FileValidator fileValidator;
    private final FileConfigurationProperties fileCollectorProperties;
    private final DownloadTargetConverter downloadTargetConverter;
    private final SourceLocationDeduplicationClient sourceLocationDeduplicationClient;
    private final WorkUnitProvider workUnitProvider;
    private final FileDeduplicator fileDeduplicator;

    @Override
    public void run(String... args) {
        while (true) {
            final WorkUnit workUnit = workUnitProvider.provideNextWorkUnit();

            final Set<String> urlsToCrawl = workUnitParser.parseSourceLocations(workUnit);

            log.info("Found {} urls in the work unit.", urlsToCrawl.size());

            Lists.partition(new LinkedList<>(urlsToCrawl), 100).stream()
                    .map(sourceLocationDeduplicationClient::deduplicateSourceLocations)
                    .map(downloadTargetConverter::convert)
                    .map(sourceDownloader::downloadToFile)
                    .map(fileValidator::validateFiles)
                    .map(hashConverter::calculateHashes)
                    .map(fileDeduplicator::deduplicateFiles)
                    .forEach(this::finalizeResult);
        }
    }

    private void finalizeResult(final List<DeduplicationResult> deduplicationResults) {
        log.info("Starting to finalize {} results.", deduplicationResults.size());

        try {
            for (DeduplicationResult deduplicationResult : deduplicationResults) {
                if (deduplicationResult.isDuplicate()) {
                    Files.delete(deduplicationResult.getFileLocation());
                } else {
                    Files.move(deduplicationResult.getFileLocation(),
                            Path.of(fileCollectorProperties.getResultFolder())
                                    .resolve(deduplicationResult.getHash() + "." + deduplicationResult.getExtension())
                    );
                }
            }
        } catch (IOException e) {
            // TODO:
            e.printStackTrace();
        }
    }
}

package com.github.filecollector.command;

import com.github.filecollector.service.deduplication.FileDeduplicator;
import com.github.filecollector.service.domain.SourceLocation;
import com.github.filecollector.service.domain.TargetLocation;
import com.github.filecollector.service.download.DownloadFinalizer;
import com.github.filecollector.service.download.SourceDownloader;
import com.github.filecollector.service.download.TargetLocationFactory;
import com.github.filecollector.service.validator.FileValidator;
import com.github.filecollector.workunit.WorkUnitManipulator;
import com.github.filecollector.workunit.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDownloaderCommand implements CommandLineRunner {

    private final SourceDownloader sourceDownloader;
    private final FileValidator fileValidator;
    private final WorkUnitManipulator workUnitManipulator;
    private final FileDeduplicator fileDeduplicator;
    private final DownloadFinalizer downloadFinalizer;
    private final Semaphore commandRateLimitingSemaphore;
    private final ExecutorService commandExecutorService;
    private final TargetLocationFactory targetLocationFactory;

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            commandRateLimitingSemaphore.acquire();

            final WorkUnit workUnit = workUnitManipulator.startWorkUnit();

            commandExecutorService.execute(() -> {
                log.info("Started processing work unit: {}.", workUnit.getId());

                final List<TargetLocation> resultFiles = workUnit.getLocations().stream()
                        .flatMap(downloadTarget -> {
                            try {
                                final SourceLocation sourceLocation = SourceLocation.builder()
                                        .location(new URI(downloadTarget))
                                        .build();
                                final TargetLocation targetLocation = targetLocationFactory.newTargetLocation(
                                        sourceLocation);

                                return sourceDownloader.downloadToFile(sourceLocation, targetLocation).stream();
                            } catch (URISyntaxException e) {
                                return Stream.empty();
                            }
                        })
                        .flatMap(targetLocation -> fileValidator.validateFile(targetLocation).stream())
                        .toList();

                log.info("Got {} successfully downloaded documents.", resultFiles.size());

                if (!resultFiles.isEmpty()) {
                    Stream.of(resultFiles)
                            .flatMap(deduplicate -> fileDeduplicator.deduplicateFiles(deduplicate).stream())
                            .forEach(downloadFinalizer::finalizeDownload);
                } else {
                    log.info("Skipping further file processing because no document was downloaded successfully.");
                }

                log.info("Finished work unit: {}.", workUnit.getId());

                workUnitManipulator.closeWorkUnit(workUnit);

                commandRateLimitingSemaphore.release();
            });
        }
    }
}

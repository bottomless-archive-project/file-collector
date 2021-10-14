package com.github.filecollector.command;

import com.github.filecollector.service.deduplication.FileDeduplicator;
import com.github.filecollector.service.domain.DownloadTarget;
import com.github.filecollector.service.download.DownloadTargetConverter;
import com.github.filecollector.service.download.DownloadTargetFinalizer;
import com.github.filecollector.service.download.SourceDownloader;
import com.github.filecollector.service.validator.DownloadTargetValidator;
import com.github.filecollector.workunit.WorkUnitManipulator;
import com.github.filecollector.workunit.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
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
    private final Semaphore commandRateLimitingSemaphore;
    private final ExecutorService commandExecutorService;

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            commandRateLimitingSemaphore.acquire();

            final WorkUnit workUnit = workUnitManipulator.startWorkUnit();

            commandExecutorService.execute(() -> {
                log.info("Started processing work unit: {}.", workUnit.getId());

                final List<DownloadTarget> resultFiles = Flux.fromIterable(workUnit.getLocations())
                        .map(downloadTargetConverter::convert)
                        .flatMap(Mono::justOrEmpty)
                        .flatMap(sourceDownloader::downloadToFile)
                        .flatMap(downloadTargetValidator::validateFiles)
                        .toStream()
                        .toList();

                log.info("Got {} successfully downloaded documents.", resultFiles.size());

                if (!resultFiles.isEmpty()) {
                    Stream.of(resultFiles)
                            .flatMap(deduplicate -> fileDeduplicator.deduplicateFiles(deduplicate).stream())
                            .forEach(downloadTargetFinalizer::finalizeDownloadTargets);
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

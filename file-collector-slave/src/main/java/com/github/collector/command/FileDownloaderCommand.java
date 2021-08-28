package com.github.collector.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.warc.service.WarcRecordStreamFactory;
import com.github.bottomlessarchive.warc.service.content.response.domain.ResponseContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecordType;
import com.github.collector.configuration.FileConfigurationProperties;
import com.github.collector.configuration.MasterServerConfigurationProperties;
import com.github.collector.service.FileDownloader;
import com.github.collector.service.FileLocationParser;
import com.github.collector.service.FileValidator;
import com.github.collector.service.ParsingContextFactory;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.view.work.response.StartWorkUnitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FileDownloaderCommand implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final FileLocationParser fileLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final FileDownloader fileDownloader;
    private final FileValidator fileValidator;
    private final FileConfigurationProperties fileCollectorProperties;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    @Override
    public void run(String... args) throws IOException, InterruptedException {
        while (true) {
            final Optional<WorkUnit> workUnit = loadNextWorkUnit();

            if(workUnit.isEmpty()) {
                Thread.sleep(600000);

                continue;
            }

            final List<String> urlsToCrawl = WarcRecordStreamFactory.<ResponseContentBlock>streamOf(
                            new URL(workUnit.get().getLocation()), List.of(WarcRecordType.RESPONSE))
                    .map(parsingContextFactory::buildParsingContext)
                    .flatMap(parsingContext -> fileLocationParser.parseLocations(parsingContext).stream())
                    .filter(this::isExpectedFileType)
                    .toList();

            // TODO: Partition it to the batches of 1000
            // TODO: Send locations for deduplication

            // TODO: Download only the non-duplicates
            final List<Path> results = urlsToCrawl.stream()
                    .flatMap(fileLocation -> fileDownloader.downloadFile(fileLocation)
                            .filter(fileValidator::validateFile).stream())
                    .toList();

            // TODO: Create the hashes for the downloaded files
            // TODO: Delete the duplicates, move the new files to the disc

            results.size();

            break;
        }
    }

    private Optional<WorkUnit> loadNextWorkUnit() throws IOException {
        final URL startWorkUnitLocation = new URL(masterServerConfigurationProperties.getMasterLocation()
                + "/work-unit/start-work");
        final StartWorkUnitResponse startWorkUnitResponse = objectMapper.readValue(
                startWorkUnitLocation, StartWorkUnitResponse.class);

        // TODO: When no more tasks, return empty optional

        return Optional.of(
                WorkUnit.builder()
                        .id(startWorkUnitResponse.getId())
                        .location(startWorkUnitResponse.getLocation())
                        .build()
        );
    }

    private boolean isExpectedFileType(final String fileLocation) {
        return fileCollectorProperties.getTypes().stream()
                .anyMatch(fileLocation::endsWith);
    }
}

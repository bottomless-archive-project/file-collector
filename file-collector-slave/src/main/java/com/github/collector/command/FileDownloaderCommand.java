package com.github.collector.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.warc.service.WarcRecordStreamFactory;
import com.github.bottomlessarchive.warc.service.content.response.domain.ResponseContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecordType;
import com.github.collector.configuration.FileConfigurationProperties;
import com.github.collector.configuration.MasterServerConfigurationProperties;
import com.github.collector.service.*;
import com.github.collector.service.domain.DeduplicationResult;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.view.document.request.DocumentDeduplicationRequest;
import com.github.collector.view.document.response.DocumentDeduplicationResponse;
import com.github.collector.view.location.request.DeduplicateDocumentLocationRequest;
import com.github.collector.view.location.response.DeduplicateDocumentLocationResponse;
import com.github.collector.view.work.response.StartWorkUnitResponse;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDownloaderCommand implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final FileLocationParser fileLocationParser;
    private final ParsingContextFactory parsingContextFactory;
    private final FileDownloader fileDownloader;
    private final FileValidator fileValidator;
    private final FileConfigurationProperties fileCollectorProperties;
    private final Sha256ChecksumProvider sha256ChecksumProvider;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    private final HttpClient client = HttpClient.newBuilder()
            .version(Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Override
    public void run(String... args) throws IOException, InterruptedException {
        while (true) {
            final Optional<WorkUnit> workUnit = loadNextWorkUnit();

            if (workUnit.isEmpty()) {
                Thread.sleep(600000);

                continue;
            }

            final Set<String> urlsToCrawl = WarcRecordStreamFactory.<ResponseContentBlock>streamOf(
                            new URL(workUnit.get().getLocation()), List.of(WarcRecordType.RESPONSE))
                    .map(parsingContextFactory::buildParsingContext)
                    .flatMap(parsingContext -> fileLocationParser.parseLocations(parsingContext).stream())
                    .filter(this::isExpectedFileType)
                    .collect(Collectors.toSet());

            log.info("Found {} urls in the work unit.", urlsToCrawl.size());

            Lists.partition(new LinkedList<>(urlsToCrawl), 2000).stream()
                    .map(this::deduplicateUrls)
                    .map(this::downloadUrls)
                    .map(this::deduplicateFiles)
                    .forEach(this::finalizeResult);
        }
    }

    private Optional<WorkUnit> loadNextWorkUnit() {
        log.info("Loading next work unit.");

        try {
            final URI startWorkUnitLocation = new URI(masterServerConfigurationProperties.getMasterLocation()
                    + "/work-unit/start-work");

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(startWorkUnitLocation)
                    .timeout(Duration.of(10, SECONDS))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            final StartWorkUnitResponse startWorkUnitResponse = objectMapper.readValue(
                    response.body(), StartWorkUnitResponse.class);

            // TODO: When no more tasks, return empty optional

            final WorkUnit workUnit = WorkUnit.builder()
                    .id(startWorkUnitResponse.getId())
                    .location(startWorkUnitResponse.getLocation())
                    .build();

            log.info("Got work unit: {}.", workUnit);

            return Optional.of(workUnit);
        } catch (Exception e) {
            e.printStackTrace();

            return Optional.empty();
        }
    }

    private boolean isExpectedFileType(final String fileLocation) {
        return fileCollectorProperties.getTypes().stream()
                .anyMatch(fileLocation::endsWith);
    }

    private List<String> deduplicateUrls(final List<String> urls) {
        log.info("Deduplication {} urls.", urls.size());

        try {
            final URI deduplicateDocumentLocations = new URI(masterServerConfigurationProperties.getMasterLocation()
                    + "/document-location");

            final String requestBody = objectMapper.writeValueAsString(
                    DeduplicateDocumentLocationRequest.builder()
                            .locations(urls)
                            .build()
            );

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(deduplicateDocumentLocations)
                    .timeout(Duration.of(10, SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Accept", "*/*")
                    .header("Content-Type", "application/json")
                    .build();

            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            final DeduplicateDocumentLocationResponse deduplicateDocumentLocationResponse =
                    objectMapper.readValue(response.body(), DeduplicateDocumentLocationResponse.class);

            log.info("From the sent urls {} was unique.", deduplicateDocumentLocationResponse.getLocations().size());

            return deduplicateDocumentLocationResponse.getLocations();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();

            return Collections.emptyList();
        }
    }

    private List<Path> downloadUrls(final List<String> urls) {
        return urls.stream()
                .parallel()
                .flatMap(fileLocation -> fileDownloader.downloadFile(fileLocation).stream())
                .filter(fileValidator::validateFile)
                .toList();
    }

    private List<DeduplicationResult> deduplicateFiles(final List<Path> paths) {
        try {
            final Map<String, Path> hashPathMap = new HashMap<>();

            for (Path path : paths) {
                try {
                    final String checksum = sha256ChecksumProvider.checksum(Files.readAllBytes(path));

                    hashPathMap.put(checksum, path);
                } catch (IOException e) {
                    // TODO: We need to handle this
                    e.printStackTrace();
                }
            }

            final URI deduplicateDocumentLocations = new URI(masterServerConfigurationProperties.getMasterLocation()
                    + "/document-location");

            final String requestBody = objectMapper.writeValueAsString(
                    DocumentDeduplicationRequest.builder()
                            .hashes(new LinkedList<>(hashPathMap.keySet()))
                            .build()
            );

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(deduplicateDocumentLocations)
                    .timeout(Duration.of(10, SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            final DocumentDeduplicationResponse documentDeduplicationResponse =
                    objectMapper.readValue(response.body(), DocumentDeduplicationResponse.class);

            hashPathMap.keySet().retainAll(documentDeduplicationResponse.getHashes());

            return paths.stream()
                    .map(path -> DeduplicationResult.builder()
                            .duplicate(!hashPathMap.containsValue(path))
                            .fileLocation(path)
                            .build())
                    .toList();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return Collections.emptyList();
        }
    }

    private void finalizeResult(final List<DeduplicationResult> deduplicationResults) {
        try {
            for (DeduplicationResult deduplicationResult : deduplicationResults) {
                if (deduplicationResult.isDuplicate()) {
                    Files.delete(deduplicationResult.getFileLocation());
                } else {
                    Files.move(deduplicationResult.getFileLocation(), Path.of(fileCollectorProperties.getResultFolder()));
                }
            }
        } catch (IOException e) {
            // TODO:
            e.printStackTrace();
        }
    }
}

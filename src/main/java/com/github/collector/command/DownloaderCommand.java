package com.github.collector.command;

import com.github.bottomlessarchive.warc.service.WarcRecordStreamFactory;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import com.github.collector.service.CommonCrawlWarcLocationFactory;
import com.github.collector.service.FileLocationParser;
import com.github.collector.service.ParsingContextFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    private final CommonCrawlWarcLocationFactory commonCrawlWarcLocationFactory;
    private final ParsingContextFactory parsingContextFactory;
    private final FileLocationParser fileLocationParser;
    private final WebClient downloaderWebClient;

    @Override
    public void run(final String... args) {
        final List<String> expectedFileExtensions = List.of("pdf", "doc", "docx");

        Flux.fromIterable(commonCrawlWarcLocationFactory.newUrls("CC-MAIN-2018-09"))
            .flatMap(insideWarcLocation -> Flux.fromStream(() -> WarcRecordStreamFactory.streamOf(insideWarcLocation)))
            .filter(WarcRecord::isResponse)
            .map(parsingContextFactory::buildParsingContext)
            .parallel()
            .flatMap(fileLocationParser::parseLocations)
            .runOn(Schedulers.boundedElastic())
            .filter(fileLocation -> expectedFileExtensions.stream()
                .anyMatch(fileLocation::endsWith)
            )
            .flatMap(fileLocation -> {
                final String id = UUID.randomUUID().toString();
                final String extension = expectedFileExtensions.stream()
                    .filter(fileLocation::endsWith)
                    .findFirst()
                    .orElse("");
                final Path targetLocation = Path.of("D:/downloader/" + id + "." + extension);

                return downloadFile(fileLocation, targetLocation)
                    .onErrorReturn(targetLocation)
                    .thenReturn(targetLocation);
            })
            .doOnNext(downloadedFile -> {
                try {
                    if (Files.exists(downloadedFile) && Files.size(downloadedFile) == 0) {
                        Files.delete(downloadedFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })
            .subscribe();
    }

    private Mono<Path> downloadFile(final String downloadTarget, final Path resultLocation) {
        log.info("Downloading file: " + downloadTarget);

        try {
            final Flux<DataBuffer> dataBufferFlux = newDownloadRequest(downloadTarget);

            return DataBufferUtils.write(dataBufferFlux, resultLocation)
                .doOnError(error -> resultLocation.toFile().delete())
                .thenReturn(resultLocation);
        } catch (final Exception e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Mono.empty();
        }
    }

    public Flux<DataBuffer> newDownloadRequest(final String downloadTarget) {
        return downloaderWebClient.get()
            .uri(downloadTarget)
            .retrieve()
            .bodyToFlux(DataBuffer.class);
    }
}

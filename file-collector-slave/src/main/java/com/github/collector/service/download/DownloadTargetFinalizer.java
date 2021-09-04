package com.github.collector.service.download;

import com.github.collector.configuration.FileConfigurationProperties;
import com.github.collector.service.domain.DeduplicationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadTargetFinalizer {

    private final FileConfigurationProperties fileConfigurationProperties;

    public void finalizeDownloadTargets(final DeduplicationResult deduplicationResult) {
        try {
            if (deduplicationResult.isDuplicate()) {
                Files.delete(deduplicationResult.getFileLocation());
            } else {
                Files.move(deduplicationResult.getFileLocation(),
                        Path.of(fileConfigurationProperties.getResultFolder())
                                .resolve(deduplicationResult.getHash() + "." + deduplicationResult.getExtension())
                );
            }
        } catch (IOException e) {
            // TODO:
            e.printStackTrace();
        }
    }
}

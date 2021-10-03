package com.github.filecollector.service.download;

import com.github.filecollector.configuration.FileConfigurationProperties;
import com.github.filecollector.service.domain.DeduplicationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadTargetFinalizer {

    private final FileConfigurationProperties fileConfigurationProperties;

    public void finalizeDownloadTargets(final DeduplicationResult deduplicationResult) {
        try {
            if (deduplicationResult.isDuplicate()) {
                deduplicationResult.getFileLocation().delete();
            } else {
                deduplicationResult.getFileLocation().move(
                        Path.of(fileConfigurationProperties.getResultFolder())
                                .resolve(deduplicationResult.getHash() + "." + deduplicationResult.getExtension())
                );
            }
        } catch (final IOException e) {
            log.error("Failed to delete or move the download target!", e);
        }
    }
}

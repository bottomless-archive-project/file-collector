package com.github.filecollector.service.download;

import com.github.filecollector.service.download.domain.SourceLocation;
import com.github.filecollector.service.download.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDownloader {

    private final CloseableHttpClient httpClient;

    public Optional<TargetLocation> downloadToFile(final SourceLocation sourceLocation,
                                                   final TargetLocation targetLocation) {
        log.info("Downloading: {}", sourceLocation.getLocation());

        final HttpGet httpGet = buildRequest(sourceLocation.getLocation());

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
             InputStream content = httpResponse.getEntity().getContent()) {
            Files.copy(content, targetLocation.getPath(), StandardCopyOption.REPLACE_EXISTING);

            return Optional.of(targetLocation);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.info("Error downloading a document: {}!", e.getMessage());
            }

            try {
                if (targetLocation.exists()) {
                    targetLocation.delete();
                }
            } catch (final Exception ex) {
                log.error("Failed to delete file on the staging location!", ex);
            }

            return Optional.empty();
        }
    }

    private HttpGet buildRequest(final URI location) {
        final HttpGet httpGet = new HttpGet(location);
        httpGet.setConfig(
                RequestConfig.custom()
                        .setMaxRedirects(10)
                        .setConnectionRequestTimeout(30000)
                        .setConnectTimeout(30000)
                        .setSocketTimeout(30000)
                        .build()
        );

        return httpGet;
    }
}

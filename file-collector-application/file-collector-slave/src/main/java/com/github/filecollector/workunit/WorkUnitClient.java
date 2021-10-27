package com.github.filecollector.workunit;

import com.github.filecollector.configuration.MasterServerConfigurationProperties;
import com.github.filecollector.workunit.domain.WorkUnit;
import com.github.filecollector.workunit.view.request.CloseWorkUnitRequest;
import com.github.filecollector.workunit.view.response.StartWorkUnitResponse;
import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.MoreBodyHandlers;
import com.github.mizosoft.methanol.MoreBodyPublishers;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitClient {

    private final HttpClient httpClient;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    @SneakyThrows
    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30000))
    public Optional<WorkUnit> startWorkUnit() {
        final URI startWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                + "/work-unit/start-work");

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(startWorkUnitLocation)
                .POST(BodyPublishers.noBody())
                .build();

        HttpResponse<StartWorkUnitResponse> startWorkUnitHttpResponse = httpClient.send(
                httpRequest, MoreBodyHandlers.ofObject(StartWorkUnitResponse.class));

        if (startWorkUnitHttpResponse.statusCode() == HttpStatus.NO_CONTENT.value()) {
            return Optional.empty();
        }

        final StartWorkUnitResponse startWorkUnitResponse = startWorkUnitHttpResponse.body();

        if (startWorkUnitResponse == null) {
            return Optional.empty();
        }

        return Optional.of(
                WorkUnit.builder()
                        .id(startWorkUnitResponse.getId())
                        .locations(startWorkUnitResponse.getLocations())
                        .build()
        );
    }

    @SneakyThrows
    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30000))
    public void closeWorkUnit(final WorkUnit workUnit) {
        final URI endWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                + "/work-unit/close-work");

        final CloseWorkUnitRequest closeWorkUnitRequest = CloseWorkUnitRequest.builder()
                .workUnitId(workUnit.getId().toString())
                .build();

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(endWorkUnitLocation)
                .POST(MoreBodyPublishers.ofObject(closeWorkUnitRequest, MediaType.APPLICATION_JSON))
                .build();

        httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
    }
}

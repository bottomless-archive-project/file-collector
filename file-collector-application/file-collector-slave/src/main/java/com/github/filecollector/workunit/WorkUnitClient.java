package com.github.filecollector.workunit;

import com.github.filecollector.configuration.MasterServerConfigurationProperties;
import com.github.filecollector.workunit.domain.WorkUnit;
import com.github.filecollector.workunit.view.request.CloseWorkUnitRequest;
import com.github.filecollector.workunit.view.response.StartWorkUnitResponse;
import com.github.mizosoft.methanol.*;
import com.github.mizosoft.methanol.adapter.jackson.JacksonAdapterFactory;
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
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitClient {

    private final HttpClient httpClient;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;
    private final BodyAdapter.Decoder decoder = JacksonAdapterFactory.createDecoder();
    private final BodyAdapter.Encoder encoder = JacksonAdapterFactory.createEncoder();

    @SneakyThrows
    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30000))
    public Optional<WorkUnit> startWorkUnit() {
        try {
            log.info("Requesting new work unit.");

            final URI startWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                    + "/work-unit/start-work");

            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(startWorkUnitLocation)
                    .POST(BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<StartWorkUnitResponse> startWorkUnitHttpResponse = httpClient.send(
                    httpRequest, info -> decoder.toObject(new TypeRef<>() {
                    }, MediaType.APPLICATION_JSON));

            if (startWorkUnitHttpResponse.statusCode() == HttpStatus.NO_CONTENT.value()) {
                log.info("Got no content as a response for the work unit request. No processable work unit found.");

                return Optional.empty();
            }

            final StartWorkUnitResponse startWorkUnitResponse = startWorkUnitHttpResponse.body();

            if (startWorkUnitResponse == null) {
                log.error("Got null as a work unit response! Status: {}!", startWorkUnitHttpResponse.statusCode());

                return Optional.empty();
            }

            log.info("Returning the result of the work unit request.");

            return Optional.of(
                    WorkUnit.builder()
                            .id(startWorkUnitResponse.getId())
                            .locations(startWorkUnitResponse.getLocations())
                            .build()
            );
        } catch (Exception e) {
            log.error("Exception while doing a start work unit request!", e);

            throw e;
        }
    }

    @SneakyThrows
    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30000))
    public void closeWorkUnit(final WorkUnit workUnit) {
        try {
            final URI endWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                    + "/work-unit/close-work");

            final CloseWorkUnitRequest closeWorkUnitRequest = CloseWorkUnitRequest.builder()
                    .workUnitId(workUnit.getId().toString())
                    .build();

            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(endWorkUnitLocation)
                    .POST(encoder.toBody(closeWorkUnitRequest, MediaType.APPLICATION_JSON))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            log.error("Exception while doing closing work unit request!", e);

            throw e;
        }
    }
}

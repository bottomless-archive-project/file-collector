package com.github.collector.service.workunit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.collector.configuration.MasterServerConfigurationProperties;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.view.work.request.CloseWorkUnitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class WorkUnitRequestFactory {

    private final ObjectMapper objectMapper;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    public HttpRequest newStartWorkUnitRequest() {
        final URI startWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                + "/work-unit/start-work");

        return HttpRequest.newBuilder()
                .uri(startWorkUnitLocation)
                .timeout(Duration.of(10, SECONDS))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
    }

    public HttpRequest newCloseWorkUnitRequest(final WorkUnit workUnit) {
        try {
            final URI startWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                    + "/work-unit/close-work");

            final String requestBody = objectMapper.writeValueAsString(
                    CloseWorkUnitRequest.builder()
                            .workUnitId(workUnit.getLocation())
                            .build()
            );

            return HttpRequest.newBuilder()
                    .uri(startWorkUnitLocation)
                    .timeout(Duration.of(10, SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize close work request!", e);
        }
    }
}

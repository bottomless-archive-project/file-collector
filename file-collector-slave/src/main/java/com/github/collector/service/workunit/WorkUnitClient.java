package com.github.collector.service.workunit;

import com.github.collector.configuration.MasterServerConfigurationProperties;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.view.work.request.CloseWorkUnitRequest;
import com.github.collector.view.work.response.StartWorkUnitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitClient {

    @Qualifier("masterWebClient")
    private final WebClient webClient;
    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    public Optional<WorkUnit> startWorkUnit() {
        final URI startWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                + "/work-unit/start-work");

        final ResponseEntity<StartWorkUnitResponse> startWorkUnitResponseResponseEntity = webClient.post()
                .uri(startWorkUnitLocation)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.empty())
                .retrieve()
                .toEntity(StartWorkUnitResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (startWorkUnitResponseResponseEntity == null || startWorkUnitResponseResponseEntity.getStatusCode()
                .equals(HttpStatus.NO_CONTENT)) {
            return Optional.empty();
        }

        final StartWorkUnitResponse startWorkUnitResponse = startWorkUnitResponseResponseEntity.getBody();

        if (startWorkUnitResponse == null) {
            return Optional.empty();
        }

        return Optional.of(
                WorkUnit.builder()
                        .id(startWorkUnitResponse.getId())
                        .location(startWorkUnitResponse.getLocation())
                        .build()
        );
    }

    public void closeWorkUnit(final WorkUnit workUnit) {
        final URI endWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                + "/work-unit/close-work");

        webClient.post()
                .uri(endWorkUnitLocation)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        CloseWorkUnitRequest.builder()
                                .workUnitId(workUnit.getId().toString())
                                .build()
                )
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(30))
                .block();
    }
}

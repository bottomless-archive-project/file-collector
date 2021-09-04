package com.github.collector.service.workunit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.view.work.response.StartWorkUnitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final WorkUnitRequestFactory workUnitRequestFactory;

    public Optional<WorkUnit> startWorkUnit() {
        try {
            final HttpRequest request = workUnitRequestFactory.newStartWorkUnitRequest();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (!hasNewWorkUnit(response)) {
                return Optional.empty();
            }

            final StartWorkUnitResponse startWorkUnitResponse = objectMapper.readValue(
                    response.body(), StartWorkUnitResponse.class);

            return Optional.of(
                    WorkUnit.builder()
                            .id(startWorkUnitResponse.getId())
                            .location(startWorkUnitResponse.getLocation())
                            .build()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return Optional.empty();
        } catch (IOException e) {
            log.error("Failed to get work unit!", e);

            return Optional.empty();
        }
    }

    private boolean hasNewWorkUnit(final HttpResponse<String> response) {
        return response.statusCode() != HttpStatus.NO_CONTENT.value();
    }

    public void closeWorkUnit(final WorkUnit workUnit) {
        try {
            final HttpRequest request = workUnitRequestFactory.newCloseWorkUnitRequest(workUnit);

            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("Failed to close work unit!", e);
        }
    }
}

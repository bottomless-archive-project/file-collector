package com.github.collector.service.workunit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.view.work.response.StartWorkUnitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Optional<WorkUnit> getNextWorkUnit() {
        try {
            final HttpRequest request = workUnitRequestFactory.newWorkUnitRequest();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            final StartWorkUnitResponse startWorkUnitResponse = objectMapper.readValue(
                    response.body(), StartWorkUnitResponse.class);

            // TODO: When no more tasks, return empty optional

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
}

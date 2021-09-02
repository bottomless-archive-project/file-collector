package com.github.collector.service.workunit;

import com.github.collector.configuration.MasterServerConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class WorkUnitRequestFactory {

    private final MasterServerConfigurationProperties masterServerConfigurationProperties;

    public HttpRequest newWorkUnitRequest() {
        final URI startWorkUnitLocation = URI.create(masterServerConfigurationProperties.getMasterLocation()
                + "/work-unit/start-work");

        return HttpRequest.newBuilder()
                .uri(startWorkUnitLocation)
                .timeout(Duration.of(10, SECONDS))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
    }
}

package com.github.collector.service.download;

import com.github.collector.service.domain.DownloadTarget;
import com.github.collector.service.domain.SourceLocation;
import com.github.collector.service.domain.TargetLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class DownloadTargetConverter {

    private final TargetLocationFactory targetLocationFactory;

    public DownloadTarget convert(final String rawSourceLocation) {
        final SourceLocation sourceLocation = SourceLocation.builder()
                .location(URI.create(rawSourceLocation))
                .build();

        final TargetLocation targetLocation = targetLocationFactory.newTargetLocation(sourceLocation);

        return DownloadTarget.builder()
                .sourceLocation(sourceLocation)
                .targetLocation(targetLocation)
                .build();
    }
}

package com.github.collector.service.download;

import com.github.collector.service.domain.DownloadTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DownloadTargetConverter {

    private final SourceLocationFactory sourceLocationFactory;
    private final TargetLocationFactory targetLocationFactory;

    public List<DownloadTarget> convert(final List<String> sourceLocations) {
        return sourceLocations.stream()
                .map(location -> sourceLocationFactory.newSourceLocation(location)
                        .map(sourceLocation -> {
                            final Path targetLocation = targetLocationFactory.newTargetLocation(sourceLocation);

                            return DownloadTarget.builder()
                                    .sourceLocation(sourceLocation)
                                    .targetLocation(targetLocation)
                                    .build();
                        })
                )
                .flatMap(Optional::stream)
                .toList();
    }
}

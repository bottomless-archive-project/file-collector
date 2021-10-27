package com.github.filecollector.service.download;

import com.github.filecollector.service.download.domain.SourceLocation;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Service
public class SourceLocationFactory {

    public Optional<SourceLocation> newSourceLocation(final String targetLocation) {
        try {
            return Optional.of(
                    SourceLocation.builder()
                            .location(new URI(targetLocation))
                            .build()
            );
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }
}

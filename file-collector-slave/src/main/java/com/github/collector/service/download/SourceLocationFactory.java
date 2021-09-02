package com.github.collector.service.download;

import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Service
public class SourceLocationFactory {

    public Optional<URL> newSourceLocation(final String locationAsString) {
        try {
            return Optional.of(new URL(locationAsString));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }
}

package com.github.filecollector.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("master")
public class MasterServerConfigurationProperties {

    private final String host;
    private final int port;

    public String getMasterLocation() {
        return "http://" + host + ":" + port;
    }
}

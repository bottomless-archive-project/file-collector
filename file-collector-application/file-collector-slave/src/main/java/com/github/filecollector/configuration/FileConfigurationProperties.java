package com.github.filecollector.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("files")
public class FileConfigurationProperties {

    private List<String> types;
    private String stageFolder;
    private String resultFolder;
}
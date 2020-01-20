package com.github.collector.configuration;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("collector")
public class FileCollectorProperties {

    private String crawlId;
    private int wardId;
    private List<String> types;
}
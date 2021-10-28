package com.github.filecollector.command.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("execution")
public class ExecutionConfigurationProperties {

    private int parallelismTarget = 0;

    public int getParallelismTarget() {
        return parallelismTarget == 0 ? Runtime.getRuntime().availableProcessors() * 2
                : parallelismTarget;
    }
}

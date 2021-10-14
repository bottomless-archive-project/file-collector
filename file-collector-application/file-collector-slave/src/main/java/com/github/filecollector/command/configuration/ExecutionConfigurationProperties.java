package com.github.filecollector.command.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
@ConfigurationProperties("execution")
public class ExecutionConfigurationProperties {

    private int parallelismTarget = 0;

    public int getParallelismTarget() {
        int finalTarget = parallelismTarget;

        if (finalTarget == 0) {
            finalTarget = Runtime.getRuntime().availableProcessors() * 2;
        }

        log.info("Initializing the application with parallelism target of {}.", finalTarget);

        return finalTarget;
    }
}

package com.github.filecollector.command.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExecutorConfiguration {

    private final ExecutionConfigurationProperties executionConfigurationProperties;

    @Bean
    public ExecutorService commandExecutorService() {
        final int parallelismTarget = executionConfigurationProperties.getParallelismTarget();

        log.info("Initializing the application with parallelism target of {}.", parallelismTarget);

        return Executors.newFixedThreadPool(parallelismTarget);
    }

    @Bean
    public Semaphore commandRateLimitingSemaphore() {
        return new Semaphore(executionConfigurationProperties.getParallelismTarget());
    }
}

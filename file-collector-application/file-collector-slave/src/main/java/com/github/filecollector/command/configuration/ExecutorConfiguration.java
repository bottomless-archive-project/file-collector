package com.github.filecollector.command.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Configuration
@RequiredArgsConstructor
public class ExecutorConfiguration {

    private final ExecutionConfigurationProperties executionConfigurationProperties;

    @Bean
    public ExecutorService commandExecutorService() {
        return Executors.newFixedThreadPool(executionConfigurationProperties.getParallelismTarget());
    }

    @Bean
    public Semaphore commandRateLimitingSemaphore() {
        return new Semaphore(executionConfigurationProperties.getParallelismTarget());
    }
}

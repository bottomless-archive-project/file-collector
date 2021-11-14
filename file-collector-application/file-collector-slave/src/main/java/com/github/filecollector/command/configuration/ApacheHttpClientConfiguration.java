package com.github.filecollector.command.configuration;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class ApacheHttpClientConfiguration {

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        return HttpClientBuilder.create()
                .setMaxConnTotal(500)
                .setRetryHandler((exception, executionCount, context) -> false)
                .setConnectionTimeToLive(1, TimeUnit.MINUTES)
                .build();
    }
}

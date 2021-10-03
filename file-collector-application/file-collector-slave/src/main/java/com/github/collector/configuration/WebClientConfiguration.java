package com.github.collector.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfiguration {

    private static final int DOWNLOADER_CLIENT_TIMEOUT = 10;

    @Bean
    public WebClient masterWebClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    public WebClient downloaderWebClient(
            @Qualifier("downloaderClientHttpConnector") final ClientHttpConnector downloaderClientHttpConnector) {
        return WebClient.builder()
                .clientConnector(downloaderClientHttpConnector)
                .build();
    }

    @Bean
    protected ClientHttpConnector downloaderClientHttpConnector(
            @Qualifier("downloaderHttpClient") final reactor.netty.http.client.HttpClient downloaderHttpClient) {
        return new ReactorClientHttpConnector(downloaderHttpClient);
    }

    @Bean
    protected reactor.netty.http.client.HttpClient downloaderHttpClient() {
        return reactor.netty.http.client.HttpClient.create()
                .responseTimeout(Duration.ofSeconds(DOWNLOADER_CLIENT_TIMEOUT))
                .followRedirect(true);
    }
}

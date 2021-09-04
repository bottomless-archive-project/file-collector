package com.github.collector.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClientConfiguration {

    private static final int DOWNLOADER_CLIENT_TIMEOUT = 10;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
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

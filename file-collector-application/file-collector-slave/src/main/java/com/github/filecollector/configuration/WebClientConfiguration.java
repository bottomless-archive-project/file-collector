package com.github.filecollector.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient masterWebClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    public HttpClient downloaderHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }
}

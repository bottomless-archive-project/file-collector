package com.github.filecollector.configuration;

import com.github.mizosoft.methanol.Methanol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public HttpClient httpClient() {
        return Methanol.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }
}

package com.github.collector.configuration;

import com.github.bottomlessarchive.commoncrawl.WarcLocationFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FileCollectorConfiguration {

    @Bean
    public WarcLocationFactory warcLocationFactory() {
        return new WarcLocationFactory();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .build();
    }
}

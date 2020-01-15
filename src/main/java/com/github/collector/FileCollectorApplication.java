package com.github.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class FileCollectorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FileCollectorApplication.class, args);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .build();
    }
}

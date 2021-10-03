package com.github.filecollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FileCollectorLoaderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FileCollectorLoaderApplication.class, args);
    }
}

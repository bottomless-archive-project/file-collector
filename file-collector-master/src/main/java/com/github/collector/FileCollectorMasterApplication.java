package com.github.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FileCollectorMasterApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FileCollectorMasterApplication.class, args);
    }
}

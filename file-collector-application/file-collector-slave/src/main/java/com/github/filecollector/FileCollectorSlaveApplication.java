package com.github.filecollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@ConfigurationPropertiesScan
public class FileCollectorSlaveApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FileCollectorSlaveApplication.class, args);
    }
}

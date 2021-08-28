package com.github.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FileCollectorSlaveApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FileCollectorSlaveApplication.class, args);
    }
}

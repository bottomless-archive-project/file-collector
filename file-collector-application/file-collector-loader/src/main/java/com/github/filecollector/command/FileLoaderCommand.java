package com.github.filecollector.command;

import com.github.filecollector.workunit.service.WorkUnitService;
import com.github.filecollector.workunit.service.domain.WorkUnit;
import com.github.filecollector.workunit.service.domain.WorkUnitStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileLoaderCommand implements CommandLineRunner {

    private final WorkUnitService workUnitService;

    @Override
    public void run(String... args) throws Exception {
        int processedLines = 0;

        log.info("Started creating work units.");

        try (BufferedReader out = new BufferedReader(new FileReader(
                "C:\\Users\\gl066f\\Downloads\\url-collection-result.txt"))) {
            boolean hasNext = true;

            final List<String> urls = new ArrayList<>(1000);
            do {
                final String urlLine = out.readLine();

                processedLines++;

                if (urlLine == null) {
                    hasNext = false;

                    continue;
                }

                urls.add(urlLine);

                if (urls.size() == 1000) {
                    log.info("Creating a new work unit. Processed url lines: {}.", processedLines);

                    workUnitService.createWorkUnit(
                            WorkUnit.builder()
                                    .id(UUID.randomUUID())
                                    .locations(urls)
                                    .status(WorkUnitStatus.CREATED)
                                    .build()
                    );

                    urls.clear();
                }
            } while (hasNext);
        }

        log.info("Finished processing the url file. Total lines processed: {}.", processedLines);
    }
}

package com.github.collector.service.workunit;

import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitProvider {

    private final WorkUnitClient workUnitClient;

    public WorkUnit provideNextWorkUnit() {
        Optional<WorkUnit> workUnit;

        do {
            log.info("Loading next work unit.");

            workUnit = workUnitClient.getNextWorkUnit();

            if (workUnit.isEmpty()) {
                log.info("No work unit found. Retrying in 60 seconds.");

                try {
                    Thread.sleep(600000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (workUnit.isEmpty());

        return workUnit.get();
    }
}

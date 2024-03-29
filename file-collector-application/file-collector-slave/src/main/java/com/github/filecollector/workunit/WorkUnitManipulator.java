package com.github.filecollector.workunit;

import com.github.filecollector.workunit.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkUnitManipulator {

    private final WorkUnitClient workUnitClient;

    public WorkUnit startWorkUnit() {
        Optional<WorkUnit> workUnit;

        do {
            log.info("Starting a new work unit.");

            workUnit = workUnitClient.startWorkUnit();

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

    public void closeWorkUnit(final WorkUnit workUnit) {
        workUnitClient.closeWorkUnit(workUnit);
    }
}

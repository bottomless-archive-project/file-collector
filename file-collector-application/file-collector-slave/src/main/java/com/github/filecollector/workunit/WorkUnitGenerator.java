package com.github.filecollector.workunit;

import com.github.filecollector.workunit.service.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SynchronousSink;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkUnitGenerator implements Consumer<SynchronousSink<WorkUnit>> {

    private final WorkUnitManipulator workUnitManipulator;

    @Override
    public void accept(final SynchronousSink<WorkUnit> workUnitSynchronousSink) {
        log.info("Work unit generator was called! Requesting new work unit.");

        final WorkUnit workUnit = workUnitManipulator.startWorkUnit();

        workUnitSynchronousSink.next(workUnit);
    }
}

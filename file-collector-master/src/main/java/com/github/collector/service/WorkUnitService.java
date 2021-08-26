package com.github.collector.service;

import com.github.collector.repository.work.WorkUnitRepository;
import com.github.collector.service.domain.WorkUnit;
import com.github.collector.service.domain.WorkUnitStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkUnitService {

    private final WorkUnitRepository workUnitRepository;

    public Optional<WorkUnit> startWorkUnit() {
        return workUnitRepository.startWorkUnit()
                .map(workUnitDatabaseEntity -> WorkUnit.builder()
                        .id(workUnitDatabaseEntity.getId())
                        .location(workUnitDatabaseEntity.getLocation())
                        .status(WorkUnitStatus.valueOf(workUnitDatabaseEntity.getStatus()))
                        .build()
                );
    }

    public void finishWorkUnit(final WorkUnit workUnit) {
        workUnitRepository.finishWorkUnit(workUnit.getId());
    }
}

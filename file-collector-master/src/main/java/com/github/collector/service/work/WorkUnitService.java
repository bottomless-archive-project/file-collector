package com.github.collector.service.work;

import com.github.collector.repository.work.WorkUnitRepository;
import com.github.collector.repository.work.domain.WorkUnitDatabaseEntity;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.service.work.domain.WorkUnitStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkUnitService {

    private final WorkUnitRepository workUnitRepository;

    public void createWorkUnit(final List<WorkUnit> workUnits) {
        final List<WorkUnitDatabaseEntity> workUnitDatabaseEntities = workUnits.stream()
                .map(workUnit -> {
                    WorkUnitDatabaseEntity workUnitDatabaseEntity = new WorkUnitDatabaseEntity();

                    workUnitDatabaseEntity.setId(workUnit.getId());
                    workUnitDatabaseEntity.setLocation(workUnit.getLocation());
                    workUnitDatabaseEntity.setStatus(workUnit.getStatus().name());

                    return workUnitDatabaseEntity;
                })
                .toList();

        workUnitRepository.createWorkUnits(workUnitDatabaseEntities);
    }

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

package com.github.filecollector.workunit.service;

import com.github.filecollector.location.service.DocumentLocationService;
import com.github.filecollector.workunit.repository.WorkUnitRepository;
import com.github.filecollector.workunit.repository.domain.WorkUnitDatabaseEntity;
import com.github.filecollector.workunit.service.domain.WorkUnit;
import com.github.filecollector.workunit.service.domain.WorkUnitStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkUnitService {

    private final WorkUnitRepository workUnitRepository;
    private final DocumentLocationService documentLocationService;

    public void createWorkUnit(final List<WorkUnit> workUnits) {
        final List<WorkUnitDatabaseEntity> workUnitDatabaseEntities = workUnits.stream()
                .map(workUnit -> {
                    WorkUnitDatabaseEntity workUnitDatabaseEntity = new WorkUnitDatabaseEntity();

                    workUnitDatabaseEntity.setId(workUnit.getId());
                    workUnitDatabaseEntity.setLocations(workUnit.getLocations());
                    workUnitDatabaseEntity.setStatus(workUnit.getStatus().name());

                    return workUnitDatabaseEntity;
                })
                .toList();

        workUnitRepository.createWorkUnits(workUnitDatabaseEntities);
    }

    public Optional<WorkUnit> startWorkUnit() {
        return workUnitRepository.startWorkUnit()
                .map(workUnitDatabaseEntity -> {
                    final List<String> uniqueUrlsInWorkUnit = documentLocationService.deduplicateDocumentLocations(
                            workUnitDatabaseEntity.getLocations());

                    return WorkUnit.builder()
                            .id(workUnitDatabaseEntity.getId())
                            .locations(uniqueUrlsInWorkUnit)
                            .status(WorkUnitStatus.valueOf(workUnitDatabaseEntity.getStatus()))
                            .build();
                });
    }

    public void finishWorkUnit(final WorkUnit workUnit) {
        workUnitRepository.finishWorkUnit(workUnit.getId());
    }
}

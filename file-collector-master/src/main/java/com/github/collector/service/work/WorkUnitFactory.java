package com.github.collector.service.work;

import com.github.collector.repository.work.WorkUnitRepository;
import com.github.collector.service.work.domain.WorkUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkUnitFactory {

    private final WorkUnitRepository workUnitRepository;

    public Optional<WorkUnit> getWorkUnit(final UUID workUnitId) {
        return workUnitRepository.findById(workUnitId)
                .map(workUnitDatabaseEntity -> WorkUnit.builder()
                        .id(workUnitDatabaseEntity.getId())
                        .location(workUnitDatabaseEntity.getLocation())
                        .underProcessing(workUnitDatabaseEntity.isUnderProcessing())
                        .build()
                );
    }
}

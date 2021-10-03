package com.github.collector.repository.work;

import com.github.collector.repository.work.domain.WorkUnitDatabaseEntity;
import com.github.collector.service.work.domain.WorkUnitStatus;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@Component
@RequiredArgsConstructor
public class WorkUnitRepository {

    private final MongoCollection<WorkUnitDatabaseEntity> documentDatabaseEntityMongoCollection;

    public void createWorkUnits(final List<WorkUnitDatabaseEntity> workUnitDatabaseEntity) {
        documentDatabaseEntityMongoCollection.insertMany(workUnitDatabaseEntity);
    }

    public Optional<WorkUnitDatabaseEntity> findById(final UUID documentId) {
        return Optional.ofNullable(
                documentDatabaseEntityMongoCollection.find(eq("_id", documentId))
                        .first()
        );
    }

    public Optional<WorkUnitDatabaseEntity> startWorkUnit() {
        return Optional.ofNullable(
                documentDatabaseEntityMongoCollection.findOneAndUpdate(
                        eq("status", WorkUnitStatus.CREATED.name()),
                        set("status", WorkUnitStatus.UNDER_PROCESSING.name())
                )
        );
    }

    public void finishWorkUnit(final UUID documentId) {
        documentDatabaseEntityMongoCollection.updateOne(eq("_id", documentId),
                set("status", WorkUnitStatus.FINISHED.name()));
    }
}

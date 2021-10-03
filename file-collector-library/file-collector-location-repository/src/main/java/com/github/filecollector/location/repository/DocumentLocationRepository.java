package com.github.filecollector.location.repository;

import com.github.filecollector.location.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private final MongoCollection<DocumentLocationDatabaseEntity> documentLocationDatabaseEntityMongoCollection;

    public List<DocumentLocationDatabaseEntity> insertDocuments(
            final List<DocumentLocationDatabaseEntity> documentLocationDatabaseEntities) {
        final List<DocumentLocationDatabaseEntity> result = new ArrayList<>(documentLocationDatabaseEntities);

        try {
            documentLocationDatabaseEntityMongoCollection.insertMany(documentLocationDatabaseEntities,
                    new InsertManyOptions().ordered(false));
        } catch (MongoBulkWriteException mongoBulkWriteException) {
            mongoBulkWriteException.getWriteErrors().stream()
                    .map(BulkWriteError::getIndex)
                    .sorted(Comparator.<Integer>naturalOrder().reversed())
                    .forEach(o -> result.remove((int) o));
        }

        return result;
    }
}

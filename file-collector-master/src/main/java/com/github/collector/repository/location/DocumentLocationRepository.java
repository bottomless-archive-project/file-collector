package com.github.collector.repository.location;

import com.github.collector.repository.location.domain.DocumentLocationDatabaseEntity;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private final MongoCollection<DocumentLocationDatabaseEntity> documentDatabaseEntityMongoCollection;

    public List<DocumentLocationDatabaseEntity> insertDocuments(
            final List<DocumentLocationDatabaseEntity> documentLocationDatabaseEntities) {

        try {
            documentDatabaseEntityMongoCollection.insertMany(documentLocationDatabaseEntities,
                    new InsertManyOptions().ordered(false));
        } catch (MongoBulkWriteException mongoBulkWriteException) {
            mongoBulkWriteException.getWriteErrors().stream()
                    .map(BulkWriteError::getIndex)
                    .sorted(Comparator.<Integer>naturalOrder().reversed())
                    .forEach(o -> documentLocationDatabaseEntities.remove((int) o));
        }

        return documentLocationDatabaseEntities;
    }
}

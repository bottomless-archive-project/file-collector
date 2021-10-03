package com.github.filecollector.repository.document;

import com.github.filecollector.repository.document.domain.DocumentDatabaseEntity;
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
public class DocumentRepository {

    private final MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection;

    public List<DocumentDatabaseEntity> insertDocuments(
            final List<DocumentDatabaseEntity> documentDatabaseEntities) {
        final List<DocumentDatabaseEntity> result = new ArrayList<>(documentDatabaseEntities);

        try {
            documentDatabaseEntityMongoCollection.insertMany(documentDatabaseEntities,
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

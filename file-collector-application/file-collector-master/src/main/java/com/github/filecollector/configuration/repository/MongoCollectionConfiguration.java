package com.github.filecollector.configuration.repository;

import com.github.filecollector.repository.document.domain.DocumentDatabaseEntity;
import com.github.filecollector.repository.location.domain.DocumentLocationDatabaseEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoCollectionConfiguration {

    @Bean
    public MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection(
            final MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("documents", DocumentDatabaseEntity.class);
    }

    @Bean
    public MongoCollection<DocumentLocationDatabaseEntity> documentLocationDatabaseEntityMongoCollection(
            final MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("documents_locations", DocumentLocationDatabaseEntity.class);
    }
}

package com.github.collector.configuration.repository;

import com.github.collector.repository.document.domain.DocumentDatabaseEntity;
import com.github.collector.repository.location.domain.DocumentLocationDatabaseEntity;
import com.github.collector.repository.work.domain.WorkUnitDatabaseEntity;
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

    @Bean
    public MongoCollection<WorkUnitDatabaseEntity> workUnitDatabaseEntityMongoCollection(
            final MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("work_units", WorkUnitDatabaseEntity.class);
    }
}

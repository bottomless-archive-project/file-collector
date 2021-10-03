package com.github.filecollector.location.configuration;

import com.github.filecollector.location.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentLocationRepositoryConfiguration {

    @Bean
    public MongoCollection<DocumentLocationDatabaseEntity> documentLocationDatabaseEntityMongoCollection(
            final MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("documents_locations", DocumentLocationDatabaseEntity.class);
    }
}

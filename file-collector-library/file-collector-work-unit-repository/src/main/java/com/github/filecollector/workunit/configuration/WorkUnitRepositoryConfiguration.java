package com.github.filecollector.workunit.configuration;

import com.github.filecollector.workunit.repository.domain.WorkUnitDatabaseEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkUnitRepositoryConfiguration {

    @Bean
    public MongoCollection<WorkUnitDatabaseEntity> workUnitDatabaseEntityMongoCollection(
            final MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("work_units", WorkUnitDatabaseEntity.class);
    }
}

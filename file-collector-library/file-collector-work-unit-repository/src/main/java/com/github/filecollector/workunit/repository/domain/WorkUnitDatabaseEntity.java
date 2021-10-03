package com.github.filecollector.workunit.repository.domain;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;
import java.util.UUID;

@Data
public class WorkUnitDatabaseEntity {

    @BsonId
    private UUID id;
    private List<String> locations;
    private String status;
}

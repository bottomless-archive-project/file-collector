package com.github.filecollector.repository.location.domain;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;

@Data
public class DocumentLocationDatabaseEntity {

    @BsonId
    private String id;
    private String location;
}

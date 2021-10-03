package com.github.filecollector.location.repository.domain;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;

@Data
public class DocumentLocationDatabaseEntity {

    @BsonId
    private String id;
    private String location;
}

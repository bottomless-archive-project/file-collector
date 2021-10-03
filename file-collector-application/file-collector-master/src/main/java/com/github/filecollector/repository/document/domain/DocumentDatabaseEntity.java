package com.github.filecollector.repository.document.domain;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;

@Data
public class DocumentDatabaseEntity {

    @BsonId
    private String id;
}

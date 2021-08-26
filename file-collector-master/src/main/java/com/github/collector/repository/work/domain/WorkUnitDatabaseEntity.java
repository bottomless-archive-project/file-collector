package com.github.collector.repository.work.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class WorkUnitDatabaseEntity {

    private UUID id;
    private String location;
    private String status;
}

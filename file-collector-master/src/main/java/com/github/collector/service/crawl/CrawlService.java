package com.github.collector.service.crawl;

import com.github.bottomlessarchive.commoncrawl.WarcLocationFactory;
import com.github.collector.service.work.WorkUnitService;
import com.github.collector.service.work.domain.WorkUnitStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.github.collector.service.work.domain.WorkUnit;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrawlService {

    private final WarcLocationFactory warcLocationFactory;
    private final WorkUnitService workUnitService;

    public void initializeCrawl(final String crawlId) {
        final List<WorkUnit> workUnits = warcLocationFactory.buildLocationStringStream(crawlId)
                .map(location -> WorkUnit.builder()
                        .id(UUID.randomUUID())
                        .location(location)
                        .status(WorkUnitStatus.CREATED)
                        .build()
                )
                .toList();

        workUnitService.createWorkUnit(workUnits);
    }
}

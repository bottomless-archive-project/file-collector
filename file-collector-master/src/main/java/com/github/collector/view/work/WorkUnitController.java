package com.github.collector.view.work;

import com.github.collector.service.work.WorkUnitFactory;
import com.github.collector.service.work.WorkUnitService;
import com.github.collector.service.work.domain.WorkUnit;
import com.github.collector.service.work.domain.WorkUnitStatus;
import com.github.collector.view.work.request.CloseWorkUnitRequest;
import com.github.collector.view.work.response.StartWorkUnitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/work-unit")
@RequiredArgsConstructor
public class WorkUnitController {

    private final WorkUnitService workUnitService;
    private final WorkUnitFactory workUnitFactory;

    @PostMapping("/start-work")
    public StartWorkUnitResponse startWorkUnit() {
        final WorkUnit workUnit = workUnitService.startWorkUnit()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT, "No more work units available!"));

        return StartWorkUnitResponse.builder()
                .id(workUnit.getId())
                .locations(workUnit.getLocations())
                .build();
    }

    @PostMapping("/close-work")
    public void closeWorkUnit(@RequestBody final CloseWorkUnitRequest closeWorkUnitRequest) {
        final WorkUnit workUnit = workUnitFactory.getWorkUnit(UUID.fromString(closeWorkUnitRequest.getWorkUnitId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown work unit!"));

        if (!WorkUnitStatus.UNDER_PROCESSING.equals(workUnit.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work unit is not under processing!");
        }

        workUnitService.finishWorkUnit(workUnit);
    }
}

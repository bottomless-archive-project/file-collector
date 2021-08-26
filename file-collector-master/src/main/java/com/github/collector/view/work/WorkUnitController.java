package com.github.collector.view.work;

import com.github.collector.service.WorkUnitFactory;
import com.github.collector.service.WorkUnitService;
import com.github.collector.service.domain.WorkUnit;
import com.github.collector.view.work.request.FinishWorkUnitRequest;
import com.github.collector.view.work.response.StartWorkUnitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/work-unit")
@RequiredArgsConstructor
public class WorkUnitController {

    private final WorkUnitService workUnitService;
    private final WorkUnitFactory workUnitFactory;

    @PostMapping
    public StartWorkUnitResponse startWorkUnit() {
        final WorkUnit workUnit = workUnitService.startWorkUnit()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT, "No more work units available!"));

        return StartWorkUnitResponse.builder()
                .id(workUnit.getId())
                .location(workUnit.getLocation())
                .build();
    }

    @PostMapping("/{workUnit}")
    public void finishWorkUnit(@PathVariable final FinishWorkUnitRequest finishWorkUnitRequest) {
        final WorkUnit workUnit = workUnitFactory.getWorkUnit(UUID.fromString(finishWorkUnitRequest.getWorkUnitId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown work unit!"));

        workUnitService.finishWorkUnit(workUnit);
    }
}

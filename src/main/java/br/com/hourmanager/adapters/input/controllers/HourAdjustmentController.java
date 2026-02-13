package br.com.hourmanager.adapters.input.controllers;

import br.com.hourmanager.adapters.input.controllers.protocols.HourAdjustmentRequest;
import br.com.hourmanager.adapters.input.controllers.protocols.HourAdjustmentResponse;
import br.com.hourmanager.application.core.domains.HourAdjustment;
import br.com.hourmanager.application.core.period.ClosurePeriodEngine;
import br.com.hourmanager.application.core.period.PeriodBounds;
import br.com.hourmanager.application.ports.input.CreateHourAdjustmentInputGateway;
import br.com.hourmanager.application.ports.input.data.HourAdjustmentInputData;
import br.com.hourmanager.application.ports.output.repositories.HourAdjustmentRepository;
import br.com.hourmanager.application.ports.output.repositories.SystemConfigRepository;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/adjustments")
public class HourAdjustmentController {

    private final CreateHourAdjustmentInputGateway createHourAdjustmentUseCase;
    private final HourAdjustmentRepository hourAdjustmentRepository;
    private final SystemConfigRepository systemConfigRepository;

    public HourAdjustmentController(CreateHourAdjustmentInputGateway createHourAdjustmentUseCase,
                                   HourAdjustmentRepository hourAdjustmentRepository,
                                   SystemConfigRepository systemConfigRepository) {
        this.createHourAdjustmentUseCase = createHourAdjustmentUseCase;
        this.hourAdjustmentRepository = hourAdjustmentRepository;
        this.systemConfigRepository = systemConfigRepository;
    }

    @PostMapping
    public ResponseEntity<HourAdjustmentResponse> create(@Valid @RequestBody HourAdjustmentRequest request) {
        HourAdjustment created = createHourAdjustmentUseCase.create(
                HourAdjustmentInputData.builder()
                        .adjustmentDate(request.getAdjustmentDate())
                        .deltaHours(request.getDeltaHours())
                        .description(request.getDescription())
                        .build()
        );
        return ResponseEntity.status(CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<HourAdjustmentResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false, defaultValue = "false") boolean periodCurrent) {

        List<HourAdjustment> adjustments;
        if (periodCurrent) {
            adjustments = listByCurrentPeriod();
        } else if (start != null && end != null) {
            adjustments = hourAdjustmentRepository.findByAdjustmentDateBetween(start, end);
        } else {
            adjustments = hourAdjustmentRepository.findAll();
        }
        return ResponseEntity.ok(adjustments.stream().map(HourAdjustmentController::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HourAdjustmentResponse> getById(@PathVariable UUID id) {
        return hourAdjustmentRepository.findById(id)
                .map(adj -> ResponseEntity.ok(toResponse(adj)))
                .orElse(ResponseEntity.notFound().build());
    }

    private List<HourAdjustment> listByCurrentPeriod() {
        return systemConfigRepository.findCurrent()
                .map(config -> {
                    PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(
                            LocalDate.now(),
                            config.getClosureStartDay(),
                            config.getClosureEndDay()
                    );
                    return hourAdjustmentRepository.findByAdjustmentDateBetween(bounds.getStart(), bounds.getEnd());
                })
                .orElse(List.of());
    }

    private static HourAdjustmentResponse toResponse(HourAdjustment adjustment) {
        return HourAdjustmentResponse.builder()
                .id(adjustment.getId() != null ? adjustment.getId().toString() : null)
                .adjustmentDate(adjustment.getAdjustmentDate())
                .deltaHours(adjustment.getDeltaHours())
                .description(adjustment.getDescription())
                .build();
    }
}

package br.com.hourmanager.adapters.input.controllers;

import br.com.hourmanager.adapters.input.controllers.protocols.HourEntryRequest;
import br.com.hourmanager.adapters.input.controllers.protocols.HourEntryResponse;
import br.com.hourmanager.application.core.domains.HourEntry;
import br.com.hourmanager.application.core.period.ClosurePeriodEngine;
import br.com.hourmanager.application.core.period.PeriodBounds;
import br.com.hourmanager.application.ports.input.CreateHourEntryInputGateway;
import br.com.hourmanager.application.ports.input.data.HourEntryInputData;
import br.com.hourmanager.application.ports.output.repositories.HourEntryRepository;
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
@RequestMapping("/api/v1/entries")
public class HourEntryController {

    private final CreateHourEntryInputGateway createHourEntryUseCase;
    private final HourEntryRepository hourEntryRepository;
    private final SystemConfigRepository systemConfigRepository;

    public HourEntryController(CreateHourEntryInputGateway createHourEntryUseCase,
                               HourEntryRepository hourEntryRepository,
                               SystemConfigRepository systemConfigRepository) {
        this.createHourEntryUseCase = createHourEntryUseCase;
        this.hourEntryRepository = hourEntryRepository;
        this.systemConfigRepository = systemConfigRepository;
    }

    @PostMapping
    public ResponseEntity<HourEntryResponse> create(@Valid @RequestBody HourEntryRequest request) {
        HourEntry created = createHourEntryUseCase.create(
                HourEntryInputData.builder()
                        .entryDate(request.getEntryDate())
                        .hours(request.getHours())
                        .description(request.getDescription())
                        .build()
        );
        return ResponseEntity.status(CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<HourEntryResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false, defaultValue = "false") boolean periodCurrent) {

        List<HourEntry> entries;
        if (periodCurrent) {
            entries = listByCurrentPeriod();
        } else if (start != null && end != null) {
            entries = hourEntryRepository.findByEntryDateBetween(start, end);
        } else {
            entries = hourEntryRepository.findAll();
        }
        return ResponseEntity.ok(entries.stream().map(HourEntryController::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HourEntryResponse> getById(@PathVariable UUID id) {
        return hourEntryRepository.findById(id)
                .map(entry -> ResponseEntity.ok(toResponse(entry)))
                .orElse(ResponseEntity.notFound().build());
    }

    private List<HourEntry> listByCurrentPeriod() {
        return systemConfigRepository.findCurrent()
                .map(config -> {
                    PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(
                            LocalDate.now(),
                            config.getClosureStartDay(),
                            config.getClosureEndDay()
                    );
                    return hourEntryRepository.findByEntryDateBetween(bounds.getStart(), bounds.getEnd());
                })
                .orElse(List.of());
    }

    private static HourEntryResponse toResponse(HourEntry entry) {
        return HourEntryResponse.builder()
                .id(entry.getId() != null ? entry.getId().toString() : null)
                .entryDate(entry.getEntryDate())
                .hours(entry.getHours())
                .description(entry.getDescription())
                .build();
    }
}

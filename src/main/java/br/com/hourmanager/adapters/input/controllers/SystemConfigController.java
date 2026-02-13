package br.com.hourmanager.adapters.input.controllers;

import br.com.hourmanager.adapters.input.controllers.protocols.SystemConfigRequest;
import br.com.hourmanager.adapters.input.controllers.protocols.SystemConfigResponse;
import br.com.hourmanager.application.ports.input.SystemConfigInputGateway;
import br.com.hourmanager.application.ports.input.data.SystemConfigInputData;
import br.com.hourmanager.application.core.domains.SystemConfig;
import br.com.hourmanager.application.ports.output.repositories.SystemConfigRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/system-config")
public class SystemConfigController {

    private final SystemConfigInputGateway saveSystemConfigUseCase;
    private final SystemConfigRepository systemConfigRepository;

    public SystemConfigController(SystemConfigInputGateway saveSystemConfigUseCase,
                                  SystemConfigRepository systemConfigRepository) {
        this.saveSystemConfigUseCase = saveSystemConfigUseCase;
        this.systemConfigRepository = systemConfigRepository;
    }

    @PutMapping
    public ResponseEntity<SystemConfigResponse> save(@Valid @RequestBody SystemConfigRequest request) {
        saveSystemConfigUseCase.save(
                SystemConfigInputData.builder()
                        .closureStartDay(request.getClosureStartDay())
                        .closureEndDay(request.getClosureEndDay())
                        .expectedWeeklyHours(request.getExpectedWeeklyHours())
                        .build()
        );
        var config = systemConfigRepository.findCurrent().orElseThrow();
        return ResponseEntity.ok(toResponse(config));
    }

    @GetMapping
    public ResponseEntity<SystemConfigResponse> getCurrent() {
        return systemConfigRepository.findCurrent()
                .map(config -> ResponseEntity.ok(toResponse(config)))
                .orElse(ResponseEntity.noContent().build());
    }

    private static SystemConfigResponse toResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .id(config.getId() != null ? config.getId().toString() : null)
                .closureStartDay(config.getClosureStartDay())
                .closureEndDay(config.getClosureEndDay())
                .expectedWeeklyHours(config.getExpectedWeeklyHours())
                .createdAt(config.getCreatedAt())
                .build();
    }
}

package br.com.hourmanager.application.core.usecases;

import br.com.hourmanager.application.core.domains.SystemConfig;
import br.com.hourmanager.application.ports.input.SystemConfigInputGateway;
import br.com.hourmanager.application.ports.input.data.SystemConfigInputData;
import br.com.hourmanager.application.ports.output.repositories.SystemConfigRepository;

import java.time.Instant;

public class SaveSystemConfigUseCase implements SystemConfigInputGateway {

    private final SystemConfigRepository systemConfigRepository;

    public SaveSystemConfigUseCase(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    @Override
    public void save(SystemConfigInputData data) {
        var existing = systemConfigRepository.findCurrent();
        if (existing.isPresent()) {
            var updated = SystemConfig.builder()
                    .id(existing.get().getId())
                    .closureStartDay(data.getClosureStartDay())
                    .closureEndDay(data.getClosureEndDay())
                    .expectedWeeklyHours(data.getExpectedWeeklyHours())
                    .createdAt(existing.get().getCreatedAt())
                    .build();
            systemConfigRepository.save(updated);
        } else {
            var created = SystemConfig.builder()
                    .closureStartDay(data.getClosureStartDay())
                    .closureEndDay(data.getClosureEndDay())
                    .expectedWeeklyHours(data.getExpectedWeeklyHours())
                    .createdAt(Instant.now())
                    .build();
            systemConfigRepository.save(created);
        }
    }
}

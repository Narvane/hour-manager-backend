package br.com.hourmanager.adapters.output.repositories;

import br.com.hourmanager.adapters.output.repositories.jpa.SystemConfigJpaRepository;
import br.com.hourmanager.adapters.output.repositories.protocols.SystemConfigEntity;
import br.com.hourmanager.application.core.domains.SystemConfig;
import br.com.hourmanager.application.ports.output.repositories.SystemConfigRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class SystemConfigJpaAdapter implements SystemConfigRepository {

    private final SystemConfigJpaRepository repository;

    @Override
    public Optional<SystemConfig> findCurrent() {
        return repository.findAll().stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public SystemConfig save(SystemConfig systemConfig) {
        SystemConfigEntity entity = SystemConfigEntity.builder()
                .id(systemConfig.getId())
                .closureStartDay(systemConfig.getClosureStartDay())
                .closureEndDay(systemConfig.getClosureEndDay())
                .expectedWeeklyHours(systemConfig.getExpectedWeeklyHours())
                .createdAt(systemConfig.getCreatedAt())
                .build();
        SystemConfigEntity persisted = repository.save(entity);
        return toDomain(persisted);
    }

    private SystemConfig toDomain(SystemConfigEntity entity) {
        return SystemConfig.builder()
                .id(entity.getId())
                .closureStartDay(entity.getClosureStartDay())
                .closureEndDay(entity.getClosureEndDay())
                .expectedWeeklyHours(entity.getExpectedWeeklyHours())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

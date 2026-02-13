package br.com.hourmanager.adapters.output.repositories;

import br.com.hourmanager.adapters.output.repositories.jpa.HourAdjustmentJpaRepository;
import br.com.hourmanager.adapters.output.repositories.protocols.HourAdjustmentEntity;
import br.com.hourmanager.application.core.domains.HourAdjustment;
import br.com.hourmanager.application.ports.output.repositories.HourAdjustmentRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class HourAdjustmentJpaAdapter implements HourAdjustmentRepository {

    private final HourAdjustmentJpaRepository repository;

    @Override
    public Optional<HourAdjustment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public HourAdjustment save(HourAdjustment hourAdjustment) {
        HourAdjustmentEntity entity = HourAdjustmentEntity.builder()
                .id(hourAdjustment.getId())
                .adjustmentDate(hourAdjustment.getAdjustmentDate())
                .deltaHours(hourAdjustment.getDeltaHours())
                .description(hourAdjustment.getDescription())
                .build();
        HourAdjustmentEntity persisted = repository.save(entity);
        return toDomain(persisted);
    }

    @Override
    public List<HourAdjustment> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<HourAdjustment> findByAdjustmentDateBetween(LocalDate start, LocalDate end) {
        return repository.findByAdjustmentDateBetweenOrderByAdjustmentDateAsc(start, end).stream()
                .map(this::toDomain)
                .toList();
    }

    private HourAdjustment toDomain(HourAdjustmentEntity entity) {
        return HourAdjustment.builder()
                .id(entity.getId())
                .adjustmentDate(entity.getAdjustmentDate())
                .deltaHours(entity.getDeltaHours())
                .description(entity.getDescription())
                .build();
    }
}

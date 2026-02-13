package br.com.hourmanager.adapters.output.repositories;

import br.com.hourmanager.adapters.output.repositories.jpa.HourEntryJpaRepository;
import br.com.hourmanager.adapters.output.repositories.protocols.HourEntryEntity;
import br.com.hourmanager.application.core.domains.HourEntry;
import br.com.hourmanager.application.ports.output.repositories.HourEntryRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class HourEntryJpaAdapter implements HourEntryRepository {

    private final HourEntryJpaRepository repository;

    @Override
    public Optional<HourEntry> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public HourEntry save(HourEntry hourEntry) {
        HourEntryEntity entity = HourEntryEntity.builder()
                .id(hourEntry.getId())
                .entryDate(hourEntry.getEntryDate())
                .hours(hourEntry.getHours())
                .description(hourEntry.getDescription())
                .build();
        HourEntryEntity persisted = repository.save(entity);
        return toDomain(persisted);
    }

    @Override
    public List<HourEntry> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<HourEntry> findByEntryDateBetween(LocalDate start, LocalDate end) {
        return repository.findByEntryDateBetweenOrderByEntryDateAsc(start, end).stream()
                .map(this::toDomain)
                .toList();
    }

    private HourEntry toDomain(HourEntryEntity entity) {
        return HourEntry.builder()
                .id(entity.getId())
                .entryDate(entity.getEntryDate())
                .hours(entity.getHours())
                .description(entity.getDescription())
                .build();
    }
}

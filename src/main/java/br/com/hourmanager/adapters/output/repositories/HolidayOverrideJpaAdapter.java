package br.com.hourmanager.adapters.output.repositories;

import br.com.hourmanager.adapters.output.repositories.protocols.HolidayOverrideEntity;
import br.com.hourmanager.application.ports.output.repositories.HolidayOverrideRepository;
import br.com.hourmanager.adapters.output.repositories.jpa.HolidayOverrideJpaRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class HolidayOverrideJpaAdapter implements HolidayOverrideRepository {

    private final HolidayOverrideJpaRepository repository;

    public HolidayOverrideJpaAdapter(HolidayOverrideJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Map<LocalDate, Boolean> getOverridesBetween(LocalDate start, LocalDate end) {
        Map<LocalDate, Boolean> result = new HashMap<>();
        repository.findByOverrideDateBetweenOrderByOverrideDateAsc(start, end)
                .forEach(e -> result.put(e.getOverrideDate(), e.isHoliday()));
        return result;
    }

    @Override
    public void setOverride(LocalDate date, boolean isHoliday) {
        repository.findByOverrideDate(date)
                .ifPresentOrElse(
                        e -> {
                            e.setHoliday(isHoliday);
                            repository.save(e);
                        },
                        () -> repository.save(HolidayOverrideEntity.builder()
                                .overrideDate(date)
                                .holiday(isHoliday)
                                .build())
                );
    }
}

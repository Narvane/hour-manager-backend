package br.com.hourmanager.adapters.output.repositories;

import br.com.hourmanager.adapters.output.repositories.jpa.PeriodAdjustmentJpaRepository;
import br.com.hourmanager.adapters.output.repositories.protocols.PeriodAdjustmentEntity;
import br.com.hourmanager.application.ports.output.repositories.PeriodAdjustmentRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
public class PeriodAdjustmentJpaAdapter implements PeriodAdjustmentRepository {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PeriodAdjustmentJpaRepository repository;

    @Override
    public Optional<BigDecimal> getAdjustment(LocalDate periodStart, LocalDate periodEnd) {
        return repository.findByPeriodStartAndPeriodEnd(periodStart, periodEnd)
                .map(PeriodAdjustmentEntity::getAdjustedHours);
    }

    @Override
    public void setAdjustment(LocalDate periodStart, LocalDate periodEnd, BigDecimal adjustedHours) {
        BigDecimal value = adjustedHours != null ? adjustedHours : ZERO;
        repository.findByPeriodStartAndPeriodEnd(periodStart, periodEnd)
                .map(existing -> {
                    existing.setAdjustedHours(value);
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(PeriodAdjustmentEntity.builder()
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .adjustedHours(value)
                        .build()));
    }
}

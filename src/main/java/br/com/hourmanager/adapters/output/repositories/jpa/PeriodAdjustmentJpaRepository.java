package br.com.hourmanager.adapters.output.repositories.jpa;

import br.com.hourmanager.adapters.output.repositories.protocols.PeriodAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PeriodAdjustmentJpaRepository extends JpaRepository<PeriodAdjustmentEntity, PeriodAdjustmentEntity.PeriodAdjustmentId> {

    Optional<PeriodAdjustmentEntity> findByPeriodStartAndPeriodEnd(LocalDate periodStart, LocalDate periodEnd);
}

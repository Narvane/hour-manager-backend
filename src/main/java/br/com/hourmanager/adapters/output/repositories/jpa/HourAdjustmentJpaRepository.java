package br.com.hourmanager.adapters.output.repositories.jpa;

import br.com.hourmanager.adapters.output.repositories.protocols.HourAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HourAdjustmentJpaRepository extends JpaRepository<HourAdjustmentEntity, UUID> {

    List<HourAdjustmentEntity> findByAdjustmentDateBetweenOrderByAdjustmentDateAsc(LocalDate start, LocalDate end);
}

package br.com.hourmanager.application.ports.output.repositories;

import br.com.hourmanager.application.core.domains.HourAdjustment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HourAdjustmentRepository {

    Optional<HourAdjustment> findById(UUID id);

    HourAdjustment save(HourAdjustment hourAdjustment);

    List<HourAdjustment> findAll();

    /** Para projeções: ajustes dentro do período (inclusive). */
    List<HourAdjustment> findByAdjustmentDateBetween(LocalDate start, LocalDate end);
}

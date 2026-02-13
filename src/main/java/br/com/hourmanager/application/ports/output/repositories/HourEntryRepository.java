package br.com.hourmanager.application.ports.output.repositories;

import br.com.hourmanager.application.core.domains.HourEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HourEntryRepository {

    Optional<HourEntry> findById(UUID id);

    HourEntry save(HourEntry hourEntry);

    List<HourEntry> findAll();

    /** Para projeções: entradas dentro do período (inclusive). */
    List<HourEntry> findByEntryDateBetween(LocalDate start, LocalDate end);
}

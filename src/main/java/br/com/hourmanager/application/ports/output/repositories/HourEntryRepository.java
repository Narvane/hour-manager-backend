package br.com.hourmanager.application.ports.output.repositories;

import br.com.hourmanager.application.core.domains.HourEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HourEntryRepository {

    Optional<HourEntry> findById(UUID id);

    HourEntry save(HourEntry hourEntry);

    void deleteById(UUID id);

    List<HourEntry> findAll();

    /** Para projeções: entradas dentro do período (inclusive). */
    List<HourEntry> findByEntryDateBetween(LocalDate start, LocalDate end);

    /** Listagem paginada por período (mais recentes primeiro). */
    HourEntryRepository.PageResult findPageByEntryDateBetween(LocalDate start, LocalDate end, int page, int size);

    record PageResult(List<HourEntry> content, long totalElements, int totalPages, int number, int size) {}
}

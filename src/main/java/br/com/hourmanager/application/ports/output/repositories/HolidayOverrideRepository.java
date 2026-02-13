package br.com.hourmanager.application.ports.output.repositories;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Overrides manuais de feriado por data.
 * Chave = data, valor = true (feriado) ou false (dia útil).
 */
public interface HolidayOverrideRepository {

    /** Overrides no intervalo [start, end]. */
    Map<LocalDate, Boolean> getOverridesBetween(LocalDate start, LocalDate end);

    /** Persiste ou atualiza o override para a data. */
    void setOverride(LocalDate date, boolean isHoliday);
}

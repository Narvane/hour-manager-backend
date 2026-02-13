package br.com.hourmanager.application.core.period;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * Limites do período de fechamento para uma data de referência.
 * Calendário é base absoluta; as datas são sempre válidas (ajustadas ao tamanho do mês).
 */
@Value
@Builder
public class PeriodBounds {

    LocalDate start;
    LocalDate end;
}

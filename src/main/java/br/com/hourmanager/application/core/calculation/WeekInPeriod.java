package br.com.hourmanager.application.core.calculation;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Segmento de semana recortado pelo período.
 * Disponibilidade = proporção da semana (ex.: 40/168) × total de horas do segmento.
 * Formato para UI: Xh trabalhadas / Yh disponíveis / Zh Totais.
 */
@Value
@Builder
public class WeekInPeriod {

    /** Início do segmento (inclusive). */
    LocalDate weekStart;

    /** Fim do segmento (inclusive). */
    LocalDate weekEnd;

    BigDecimal totalWorked;
    BigDecimal totalAdjusted;
    BigDecimal balance;

    /** Reservado (não usado para disponibilidade). */
    int workingDaysCount;

    /** Proporção (expectativa/168) × totalSegmentHours. */
    BigDecimal hoursAvailable;

    /** Expectativa em semana cheia (ex.: 40h). */
    BigDecimal baseWeeklyHours;

    /** Total de horas do segmento (24 × dias). Ex.: 7 dias → 168h. */
    BigDecimal totalSegmentHours;
}

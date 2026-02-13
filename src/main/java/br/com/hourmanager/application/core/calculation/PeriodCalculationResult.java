package br.com.hourmanager.application.core.calculation;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Resultado completo do cálculo: totais do período e distribuição por semana natural.
 */
@Value
@Builder
public class PeriodCalculationResult {

    PeriodBalance summary;
    List<WeekInPeriod> weeks;
}

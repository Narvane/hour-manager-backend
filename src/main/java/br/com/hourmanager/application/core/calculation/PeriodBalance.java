package br.com.hourmanager.application.core.calculation;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Resultado derivado do cálculo do período: totais e saldo.
 * Nunca persistido; sempre calculado a partir de entradas e ajustes.
 */
@Value
@Builder
public class PeriodBalance {

    /** Soma das horas das entradas no período. */
    BigDecimal totalWorked;

    /** Soma dos deltas dos ajustes no período. */
    BigDecimal totalAdjusted;

    /** Saldo geral do período: totalWorked + totalAdjusted. */
    BigDecimal balance;

    public static PeriodBalance of(BigDecimal totalWorked, BigDecimal totalAdjusted) {
        BigDecimal balance = totalWorked.add(totalAdjusted);
        return PeriodBalance.builder()
                .totalWorked(totalWorked)
                .totalAdjusted(totalAdjusted)
                .balance(balance)
                .build();
    }
}

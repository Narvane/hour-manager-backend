package br.com.hourmanager.application.core.projection;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Objeto final da projeção para o dashboard.
 * Consumível pelo frontend; apenas dados derivados (nunca persistido).
 */
@Value
@Builder
public class DashboardProjection {

    /** Informações do período atual. */
    PeriodInfo period;

    /** Totais calculados do período. */
    TotalsInfo totals;

    /** Progresso no tempo dentro do período. */
    ProgressInfo progress;

    /** Semanas naturais dentro do período com distribuição de horas. */
    List<WeekInfo> weeks;

    /** Projeção até o fim do período e status da meta (null se não houver meta configurada). */
    GoalProjectionInfo goalProjection;

    @Value
    @Builder
    public static class PeriodInfo {
        LocalDate start;
        LocalDate end;
        long totalDays;
    }

    @Value
    @Builder
    public static class TotalsInfo {
        BigDecimal totalWorked;
        BigDecimal totalAdjusted;
        BigDecimal balance;
        /** Proporção (expectativa/168) × (24×30) — horas máximas mês cheio (30 dias). */
        BigDecimal fullMonthMaxHours;
        /** Horas disponíveis no período (proporção da semana em cada segmento). */
        BigDecimal availableHoursInPeriod;
    }

    @Value
    @Builder
    public static class ProgressInfo {
        /** Dias já decorridos desde o início do período (até a data de referência, limitado ao período). */
        long daysElapsed;
        long totalDays;
        /** Entre 0 e 1: fração do período já decorrida. */
        double percentageElapsed;
    }

    /** Formato para UI: Xh trabalhadas / Yh disponíveis / Zh Totais + indicadores de dias. */
    @Value
    @Builder
    public static class WeekInfo {
        LocalDate weekStart;
        LocalDate weekEnd;
        BigDecimal totalWorked;
        BigDecimal totalAdjusted;
        BigDecimal balance;
        int workingDaysCount;
        /** Horas disponíveis no segmento (proporção da semana). */
        BigDecimal hoursAvailable;
        BigDecimal baseWeeklyHours;
        /** Total de horas do segmento (24 × dias). */
        BigDecimal totalSegmentHours;
        List<DayInWeek> days;
    }

    /** Um dia na linha: dia da semana em cima, número do dia dentro da bolinha; feriado/passado. */
    @Value
    @Builder
    public static class DayInWeek {
        LocalDate date;
        /** Rótulo do dia da semana: Seg, Ter, Qua, Qui, Sex, Sáb, Dom. */
        String weekdayLabel;
        /** Dia do mês (1–31) para exibir dentro da bolinha. */
        int dayOfMonth;
        /** Se o dia já passou (data de referência); dias passados ficam apagados e não clicáveis. */
        boolean past;
        /** Feriado efetivo (nacional ou override). */
        boolean holiday;
        /** Se é override do usuário (marcar/desmarcar feriado). */
        boolean userOverride;
    }

    @Value
    @Builder
    public static class GoalProjectionInfo {
        /** Ritmo atual: saldo / dias decorridos (horas por dia). */
        BigDecimal currentRatePerDay;
        /** Projeção do saldo ao final do período mantendo o ritmo atual. */
        BigDecimal projectedBalanceAtEnd;
        /** Meta de horas no período (expectativa = semanas no período × horas/semana). */
        BigDecimal targetHours;
        /** Status: atingível, em risco ou impossível. */
        GoalStatus goalStatus;
    }
}

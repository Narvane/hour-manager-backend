package br.com.hourmanager.application.core.projection;

import br.com.hourmanager.application.core.calculation.PeriodCalculationResult;
import br.com.hourmanager.application.core.calculation.PeriodCalculationService;
import br.com.hourmanager.application.core.calculation.WeekInPeriod;
import br.com.hourmanager.application.core.period.PeriodBounds;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Camada de projeção para o dashboard.
 * Consome apenas o serviço de cálculo; não acessa repositórios nem persiste nada.
 */
public class DashboardProjectionService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 2;
    /** 168h = semana cheia; 720h = 30 dias (mês cheio para proporção). */
    private static final BigDecimal HOURS_IN_FULL_WEEK = new BigDecimal("168");
    private static final BigDecimal HOURS_IN_FULL_MONTH = new BigDecimal("720");
    /** Abaixo de 70% da meta: impossível. Entre 70% e 100%: em risco. >= 100%: atingível. */
    private static final BigDecimal RISK_THRESHOLD = new BigDecimal("0.70");

    private final PeriodCalculationService periodCalculationService;

    public DashboardProjectionService(PeriodCalculationService periodCalculationService) {
        this.periodCalculationService = periodCalculationService;
    }

    private static final String[] WEEKDAY_LABELS = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};

    /**
     * Gera a projeção completa para o dashboard.
     *
     * @param bounds               início e fim do período
     * @param referenceDate        data de referência (ex.: hoje) para cálculo do progresso
     * @param expectedWeeklyHours  expectativa de horas por semana (opcional)
     * @param effectiveHolidays    feriados efetivos (nacionais + overrides) para cálculo de disponibilidade
     * @param overrideDates       datas que possuem override manual (para flag isUserOverride na UI)
     * @return objeto pronto para o frontend
     */
    public DashboardProjection project(PeriodBounds bounds, LocalDate referenceDate, BigDecimal expectedWeeklyHours,
                                      Set<LocalDate> effectiveHolidays, Set<LocalDate> overrideDates) {
        PeriodCalculationResult result = periodCalculationService.computeWithWeeklyBreakdown(
                bounds, expectedWeeklyHours, effectiveHolidays != null ? effectiveHolidays : Set.of());

        long totalDays = ChronoUnit.DAYS.between(bounds.getStart(), bounds.getEnd()) + 1;
        long daysElapsed;
        if (referenceDate.isBefore(bounds.getStart())) {
            daysElapsed = 0;
        } else if (referenceDate.isAfter(bounds.getEnd())) {
            daysElapsed = totalDays;
        } else {
            daysElapsed = ChronoUnit.DAYS.between(bounds.getStart(), referenceDate) + 1;
        }
        double percentageElapsed = totalDays > 0 ? (double) daysElapsed / totalDays : 0;

        DashboardProjection.PeriodInfo period = DashboardProjection.PeriodInfo.builder()
                .start(bounds.getStart())
                .end(bounds.getEnd())
                .totalDays(totalDays)
                .build();


        DashboardProjection.ProgressInfo progress = DashboardProjection.ProgressInfo.builder()
                .daysElapsed(daysElapsed)
                .totalDays(totalDays)
                .percentageElapsed(percentageElapsed)
                .build();

        Set<LocalDate> overrides = overrideDates != null ? overrideDates : Set.of();
        List<DashboardProjection.WeekInfo> weeks = result.getWeeks().stream()
                .map(w -> toWeekInfo(w, referenceDate, effectiveHolidays != null ? effectiveHolidays : Set.of(), overrides))
                .collect(Collectors.toList());

        BigDecimal totalAvailable = result.getWeeks().stream()
                .map(WeekInPeriod::getHoursAvailable)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fullMonthMaxHours = expectedWeeklyHours != null && expectedWeeklyHours.compareTo(ZERO) > 0
                ? expectedWeeklyHours.multiply(HOURS_IN_FULL_MONTH).divide(HOURS_IN_FULL_WEEK, SCALE, RoundingMode.HALF_UP)
                : ZERO;

        DashboardProjection.TotalsInfo totalsWithAvailability = DashboardProjection.TotalsInfo.builder()
                .totalWorked(result.getSummary().getTotalWorked())
                .totalAdjusted(result.getSummary().getTotalAdjusted())
                .balance(result.getSummary().getBalance())
                .fullMonthMaxHours(fullMonthMaxHours)
                .availableHoursInPeriod(totalAvailable != null ? totalAvailable.setScale(SCALE, RoundingMode.HALF_UP) : ZERO)
                .build();

        DashboardProjection.GoalProjectionInfo goalProjection = buildGoalProjection(
                result.getSummary().getBalance(),
                daysElapsed,
                totalDays,
                totalAvailable,
                expectedWeeklyHours
        );

        return DashboardProjection.builder()
                .period(period)
                .totals(totalsWithAvailability)
                .progress(progress)
                .weeks(weeks)
                .goalProjection(goalProjection)
                .build();
    }

    /**
     * Analisa ritmo atual, projeta saldo ao fim do período e determina status da meta.
     * Meta = soma da disponibilidade dos segmentos (dias úteis × expectativa/5).
     */
    private DashboardProjection.GoalProjectionInfo buildGoalProjection(
            BigDecimal currentBalance,
            long daysElapsed,
            long totalDays,
            BigDecimal totalAvailableHours,
            BigDecimal expectedWeeklyHours) {

        if (expectedWeeklyHours == null || expectedWeeklyHours.compareTo(ZERO) <= 0 || totalAvailableHours == null || totalAvailableHours.compareTo(ZERO) <= 0) {
            return null;
        }

        BigDecimal targetHours = totalAvailableHours.setScale(SCALE, RoundingMode.HALF_UP);

        if (daysElapsed <= 0) {
            return DashboardProjection.GoalProjectionInfo.builder()
                    .currentRatePerDay(ZERO)
                    .projectedBalanceAtEnd(currentBalance)
                    .targetHours(targetHours)
                    .goalStatus(GoalStatus.EM_RISCO)
                    .build();
        }

        BigDecimal ratePerDay = currentBalance.divide(BigDecimal.valueOf(daysElapsed), SCALE, RoundingMode.HALF_UP);
        BigDecimal projectedBalanceAtEnd = ratePerDay.multiply(BigDecimal.valueOf(totalDays)).setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal ratio = targetHours.compareTo(ZERO) > 0
                ? projectedBalanceAtEnd.divide(targetHours, 4, RoundingMode.HALF_UP)
                : ZERO;

        GoalStatus status;
        if (ratio.compareTo(BigDecimal.ONE) >= 0) {
            status = GoalStatus.ATINGIVEL;
        } else if (ratio.compareTo(RISK_THRESHOLD) >= 0) {
            status = GoalStatus.EM_RISCO;
        } else {
            status = GoalStatus.IMPOSSIVEL;
        }

        return DashboardProjection.GoalProjectionInfo.builder()
                .currentRatePerDay(ratePerDay)
                .projectedBalanceAtEnd(projectedBalanceAtEnd)
                .targetHours(targetHours)
                .goalStatus(status)
                .build();
    }

    private DashboardProjection.WeekInfo toWeekInfo(WeekInPeriod w, LocalDate referenceDate, Set<LocalDate> effectiveHolidays, Set<LocalDate> overrideDates) {
        LocalDate segStart = w.getWeekStart();
        LocalDate segEnd = w.getWeekEnd();
        List<DashboardProjection.DayInWeek> days = new ArrayList<>();
        for (LocalDate d = segStart; !d.isAfter(segEnd); d = d.plusDays(1)) {
            int dow = d.getDayOfWeek().getValue();
            int index = dow % 7;
            String weekdayLabel = WEEKDAY_LABELS[index];
            boolean past = d.isBefore(referenceDate);
            boolean holiday = effectiveHolidays.contains(d);
            boolean userOverride = overrideDates.contains(d);
            days.add(DashboardProjection.DayInWeek.builder()
                    .date(d)
                    .weekdayLabel(weekdayLabel)
                    .dayOfMonth(d.getDayOfMonth())
                    .past(past)
                    .holiday(holiday)
                    .userOverride(userOverride)
                    .build());
        }
        return DashboardProjection.WeekInfo.builder()
                .weekStart(segStart)
                .weekEnd(segEnd)
                .totalWorked(w.getTotalWorked() != null ? w.getTotalWorked() : BigDecimal.ZERO)
                .totalAdjusted(w.getTotalAdjusted() != null ? w.getTotalAdjusted() : BigDecimal.ZERO)
                .balance(w.getBalance() != null ? w.getBalance() : BigDecimal.ZERO)
                .workingDaysCount(w.getWorkingDaysCount())
                .hoursAvailable(w.getHoursAvailable() != null ? w.getHoursAvailable() : BigDecimal.ZERO)
                .baseWeeklyHours(w.getBaseWeeklyHours() != null ? w.getBaseWeeklyHours() : BigDecimal.ZERO)
                .totalSegmentHours(w.getTotalSegmentHours() != null ? w.getTotalSegmentHours() : BigDecimal.ZERO)
                .days(days)
                .build();
    }
}

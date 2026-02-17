package br.com.hourmanager.application.core.calculation;

import br.com.hourmanager.application.core.domains.HourEntry;
import br.com.hourmanager.application.core.period.PeriodBounds;
import br.com.hourmanager.application.ports.output.repositories.HourEntryRepository;
import br.com.hourmanager.application.ports.output.repositories.PeriodAdjustmentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Serviço central de cálculo do período.
 * Total trabalhado = entradas; total ajustado = um valor por período (slider); saldo = trabalhado + ajustado.
 * Ajuste não aparece nas barras semanais.
 */
public class PeriodCalculationService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final HourEntryRepository hourEntryRepository;
    private final PeriodAdjustmentRepository periodAdjustmentRepository;

    public PeriodCalculationService(HourEntryRepository hourEntryRepository,
                                   PeriodAdjustmentRepository periodAdjustmentRepository) {
        this.hourEntryRepository = hourEntryRepository;
        this.periodAdjustmentRepository = periodAdjustmentRepository;
    }

    /**
     * Calcula total trabalhado (entradas), total ajustado (valor do período) e saldo.
     */
    public PeriodBalance compute(PeriodBounds bounds) {
        List<HourEntry> entries = hourEntryRepository.findByEntryDateBetween(bounds.getStart(), bounds.getEnd());

        BigDecimal totalWorked = entries.stream()
                .map(HourEntry::getHours)
                .filter(h -> h != null)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal totalAdjusted = periodAdjustmentRepository.getAdjustment(bounds.getStart(), bounds.getEnd())
                .orElse(ZERO);

        return PeriodBalance.of(totalWorked, totalAdjusted);
    }

    private static final int SCALE = 2;
    /** Semana de 7 dias = 168h; proporção (ex.: 40/168) é mantida em qualquer segmento. */
    private static final BigDecimal HOURS_IN_FULL_WEEK = new BigDecimal("168");

    /**
     * Calcula totais do período e distribuição por segmentos de semana recortados pelo período.
     * Disponibilidade por segmento: proporção da semana (expectativaSemanal / 168) × total de horas do segmento (24 × dias).
     * Ex.: 40h em semana cheia = 23,81%; em semana de 4 dias (96h) → 22,86h disponíveis.
     *
     * @param bounds              início e fim do período
     * @param expectedWeeklyHours expectativa em semana cheia (ex.: 40); opcional (null → disponibilidade 0)
     * @param holidays            (não usado para disponibilidade; mantido para compatibilidade)
     */
    public PeriodCalculationResult computeWithWeeklyBreakdown(PeriodBounds bounds, BigDecimal expectedWeeklyHours, Set<LocalDate> holidays) {
        List<HourEntry> entries = hourEntryRepository.findByEntryDateBetween(bounds.getStart(), bounds.getEnd());

        List<PeriodWeekSegments.SegmentBounds> segments = PeriodWeekSegments.segmentsWithin(bounds);
        List<WeekInPeriod> weeks = new ArrayList<>();

        for (PeriodWeekSegments.SegmentBounds seg : segments) {
            LocalDate segStart = seg.start();
            LocalDate segEnd = seg.end();

            BigDecimal worked = entries.stream()
                    .filter(e -> !e.getEntryDate().isBefore(segStart) && !e.getEntryDate().isAfter(segEnd))
                    .map(HourEntry::getHours)
                    .filter(h -> h != null)
                    .reduce(ZERO, BigDecimal::add);
            // Ajuste não aparece por semana; só no total do período
            weeks.add(WeekInPeriod.builder()
                    .weekStart(segStart)
                    .weekEnd(segEnd)
                    .totalWorked(worked)
                    .totalAdjusted(ZERO)
                    .balance(worked)
                    .workingDaysCount(0)
                    .hoursAvailable(computeHoursAvailable(expectedWeeklyHours, segStart, segEnd))
                    .baseWeeklyHours(expectedWeeklyHours != null ? expectedWeeklyHours : ZERO)
                    .totalSegmentHours(BigDecimal.valueOf(24 * (ChronoUnit.DAYS.between(segStart, segEnd) + 1)))
                    .build());
        }

        BigDecimal totalWorked = entries.stream()
                .map(HourEntry::getHours)
                .filter(h -> h != null)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalAdjusted = periodAdjustmentRepository.getAdjustment(bounds.getStart(), bounds.getEnd())
                .orElse(ZERO);
        PeriodBalance summary = PeriodBalance.of(totalWorked, totalAdjusted);

        return PeriodCalculationResult.builder()
                .summary(summary)
                .weeks(weeks)
                .build();
    }

    private BigDecimal computeHoursAvailable(BigDecimal expectedWeeklyHours, LocalDate segStart, LocalDate segEnd) {
        long segmentDays = ChronoUnit.DAYS.between(segStart, segEnd) + 1;
        BigDecimal totalSegmentHours = BigDecimal.valueOf(24 * segmentDays);
        if (expectedWeeklyHours == null || expectedWeeklyHours.compareTo(ZERO) <= 0) {
            return ZERO;
        }
        return expectedWeeklyHours
                .divide(HOURS_IN_FULL_WEEK, SCALE + 2, RoundingMode.HALF_UP)
                .multiply(totalSegmentHours)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }
}

package br.com.hourmanager.application.core.calculation;

import br.com.hourmanager.application.core.period.PeriodBounds;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gera segmentos de semana (domingo a sábado) recortados pelo período.
 * Ex.: período 21 jan – 20 fev → 21–25 jan, 26 jan–1 fev, 2–8 fev, 9–15 fev, 16–20 fev.
 */
public final class PeriodWeekSegments {

    private PeriodWeekSegments() {
    }

    /**
     * Retorna lista de [start, end] (inclusive) onde cada segmento é uma semana
     * domingo–sábado, recortada pelos limites do período.
     */
    public static List<SegmentBounds> segmentsWithin(PeriodBounds period) {
        LocalDate periodStart = period.getStart();
        LocalDate periodEnd = period.getEnd();
        List<SegmentBounds> result = new ArrayList<>();

        LocalDate segmentStart = periodStart;
        while (!segmentStart.isAfter(periodEnd)) {
            LocalDate segmentEnd = endOfWeekSaturday(segmentStart);
            if (segmentEnd.isAfter(periodEnd)) {
                segmentEnd = periodEnd;
            }
            result.add(new SegmentBounds(segmentStart, segmentEnd));
            segmentStart = segmentEnd.plusDays(1);
        }
        return result;
    }

    /** Sábado da semana (dom–sáb) que contém a data. */
    private static LocalDate endOfWeekSaturday(LocalDate date) {
        int daysSinceSunday = date.getDayOfWeek() == DayOfWeek.SUNDAY ? 0 : date.getDayOfWeek().getValue();
        LocalDate sunday = date.minusDays(daysSinceSunday);
        return sunday.plusDays(6);
    }

    public record SegmentBounds(LocalDate start, LocalDate end) {
    }
}

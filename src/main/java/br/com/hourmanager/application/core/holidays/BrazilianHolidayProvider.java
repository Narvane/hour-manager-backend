package br.com.hourmanager.application.core.holidays;

import java.time.LocalDate;
import java.time.Year;
import java.util.HashSet;
import java.util.Set;

/**
 * Feriados nacionais do Brasil que afetam dias úteis.
 * Apenas os que tipicamente são não-trabalho (Confraternização, Carnaval, Sexta Santa, etc.).
 */
public final class BrazilianHolidayProvider {

    private BrazilianHolidayProvider() {
    }

    /**
     * Retorna os feriados nacionais no intervalo [start, end].
     */
    public static Set<LocalDate> getHolidaysBetween(LocalDate start, LocalDate end) {
        Set<LocalDate> result = new HashSet<>();
        int startYear = start.getYear();
        int endYear = end.getYear();
        for (int y = startYear; y <= endYear; y++) {
            result.addAll(getHolidaysForYear(Year.of(y)));
        }
        result.removeIf(d -> d.isBefore(start) || d.isAfter(end));
        return result;
    }

    private static Set<LocalDate> getHolidaysForYear(Year year) {
        Set<LocalDate> set = new HashSet<>();
        int y = year.getValue();
        // Fixos
        set.add(LocalDate.of(y, 1, 1));   // Confraternização Universal
        set.add(LocalDate.of(y, 4, 21));   // Tiradentes
        set.add(LocalDate.of(y, 5, 1));   // Dia do Trabalho
        set.add(LocalDate.of(y, 9, 7));   // Independência
        set.add(LocalDate.of(y, 10, 12)); // N. Sra. Aparecida
        set.add(LocalDate.of(y, 11, 2));  // Finados
        set.add(LocalDate.of(y, 11, 15)); // Proclamação da República
        set.add(LocalDate.of(y, 11, 20)); // Consciência Negra
        set.add(LocalDate.of(y, 12, 25)); // Natal
        // Móveis: Carnaval (47 dias antes do Domingo de Páscoa), Sexta-feira Santa, Corpus Christi
        LocalDate easter = computeEaster(y);
        set.add(easter.minusDays(47));     // Terça de Carnaval
        set.add(easter.minusDays(2));      // Sexta-feira Santa
        set.add(easter.plusDays(60));      // Corpus Christi
        return set;
    }

    /** Cálculo do Domingo de Páscoa (algoritmo de Anonymous Gregorian). */
    private static LocalDate computeEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}

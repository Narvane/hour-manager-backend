package br.com.hourmanager.application.core.calculation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Calcula quantidade de dias úteis em um intervalo (inclusive).
 * Considera fim de semana (sábado e domingo) como não úteis.
 * Opcionalmente exclui feriados.
 */
public final class WorkingDaysCalculator {

    private WorkingDaysCalculator() {
    }

    /**
     * Conta dias úteis (segunda a sexta) em [start, end], inclusive.
     *
     * @param start    início do intervalo
     * @param end      fim do intervalo
     * @param holidays feriados a excluir (pode ser null ou vazio)
     * @return quantidade de dias úteis
     */
    public static int countWorkingDays(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
        int count = 0;
        LocalDate d = start;
        while (!d.isAfter(end)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                if (holidays == null || !holidays.contains(d)) {
                    count++;
                }
            }
            d = d.plusDays(1);
        }
        return count;
    }

    /**
     * Conta dias úteis sem considerar feriados.
     */
    public static int countWorkingDays(LocalDate start, LocalDate end) {
        return countWorkingDays(start, end, null);
    }
}

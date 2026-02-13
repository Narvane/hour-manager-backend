package br.com.hourmanager.application.core.period;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Engine de cálculo do período de fechamento.
 * Dado o dia atual e a configuração (dia início / dia fim), calcula início e fim do período atual.
 * Calendário é base absoluta; a lógica não depende de nenhuma outra camada.
 */
public final class ClosurePeriodEngine {

    private ClosurePeriodEngine() {
    }

    /**
     * Calcula o período de fechamento que contém a data de referência.
     *
     * @param referenceDate   data de referência (ex.: hoje)
     * @param closureStartDay dia do mês em que o período inicia (1–31)
     * @param closureEndDay   dia do mês em que o período termina (1–31)
     * @return início e fim do período; as datas são ajustadas quando o dia não existe no mês (ex.: 31 em fevereiro)
     */
    public static PeriodBounds computePeriodContaining(
            LocalDate referenceDate,
            int closureStartDay,
            int closureEndDay) {

        int day = referenceDate.getDayOfMonth();
        YearMonth refMonth = YearMonth.from(referenceDate);
        int year = referenceDate.getYear();

        if (closureStartDay > closureEndDay) {
            // Período atravessa dois meses (ex.: 21 a 20)
            if (day >= closureStartDay) {
                // Estamos na “segunda metade” do período: início no mês atual, fim no próximo
                LocalDate start = safeDate(refMonth, closureStartDay);
                LocalDate end = safeDate(refMonth.plusMonths(1), closureEndDay);
                return PeriodBounds.builder().start(start).end(end).build();
            } else {
                // Estamos na “primeira metade”: início no mês anterior, fim no atual
                LocalDate start = safeDate(refMonth.minusMonths(1), closureStartDay);
                LocalDate end = safeDate(refMonth, closureEndDay);
                return PeriodBounds.builder().start(start).end(end).build();
            }
        } else {
            // Período dentro do mesmo mês (ex.: 1 a 31)
            LocalDate start = safeDate(refMonth, closureStartDay);
            LocalDate end = safeDate(refMonth, closureEndDay);
            return PeriodBounds.builder().start(start).end(end).build();
        }
    }

    /**
     * Dia do mês ajustado ao tamanho do mês (ex.: 31 em fev vira 28/29).
     */
    private static LocalDate safeDate(YearMonth yearMonth, int dayOfMonth) {
        int maxDay = yearMonth.lengthOfMonth();
        int day = Math.min(dayOfMonth, maxDay);
        return yearMonth.atDay(day);
    }
}

package br.com.hourmanager.application.core.calculation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingDaysCalculatorTest {

    @Test
    @DisplayName("21–25 jan 2025: ter, qua, qui, sex, sáb → 4 dias úteis")
    void countWorkingDays_21to25Jan_fourWeekdays() {
        int count = WorkingDaysCalculator.countWorkingDays(
                LocalDate.of(2025, 1, 21),
                LocalDate.of(2025, 1, 25)
        );
        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("Com 2 feriados (seg e ter): 21–25 jan → 3 dias úteis")
    void countWorkingDays_withHolidays_threeWorkingDays() {
        Set<LocalDate> holidays = Set.of(
                LocalDate.of(2025, 1, 20),
                LocalDate.of(2025, 1, 21)
        );
        int count = WorkingDaysCalculator.countWorkingDays(
                LocalDate.of(2025, 1, 21),
                LocalDate.of(2025, 1, 25),
                holidays
        );
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Semana completa seg–sex = 5 dias úteis")
    void countWorkingDays_fullWeek_five() {
        int count = WorkingDaysCalculator.countWorkingDays(
                LocalDate.of(2025, 1, 20),
                LocalDate.of(2025, 1, 26)
        );
        assertThat(count).isEqualTo(5);
    }
}

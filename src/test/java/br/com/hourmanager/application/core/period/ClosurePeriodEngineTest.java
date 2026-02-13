package br.com.hourmanager.application.core.period;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ClosurePeriodEngineTest {

    private static final int START_21 = 21;
    private static final int END_20 = 20;

    @Nested
    @DisplayName("Período atravessando meses (startDay 21, endDay 20)")
    class PeriodSpanningMonths {

        @Test
        @DisplayName("Mês com 28 dias (fev): referência no início do período -> jan 21 a fev 20")
        void feb28_referenceInFirstHalf_periodIsPreviousMonthStartToCurrentMonthEnd() {
            LocalDate ref = LocalDate.of(2023, 2, 15); // fev 15 (ano não bissexto)
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 1, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 2, 20));
        }

        @Test
        @DisplayName("Mês com 28 dias (fev): referência no fim do período -> fev 21 a mar 20")
        void feb28_referenceInSecondHalf_periodIsCurrentMonthStartToNextMonthEnd() {
            LocalDate ref = LocalDate.of(2023, 2, 25);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 2, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 3, 20));
        }

        @Test
        @DisplayName("Fev bissexto: dia 29 pertence ao período fev 21 - mar 20")
        void feb29_leapYear_periodEndsNextMonth() {
            LocalDate ref = LocalDate.of(2024, 2, 29);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2024, 2, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2024, 3, 20));
        }

        @Test
        @DisplayName("Mês com 30 dias (abr): referência antes do dia 21 -> mar 21 a abr 20")
        void month30_referenceBeforeStart_periodFromPreviousMonth() {
            LocalDate ref = LocalDate.of(2023, 4, 10);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 3, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 4, 20));
        }

        @Test
        @DisplayName("Mês com 30 dias (abr): referência no dia 21 ou depois -> abr 21 a mai 20")
        void month30_referenceOnOrAfterStart_periodToNextMonth() {
            LocalDate ref = LocalDate.of(2023, 4, 25);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 4, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 5, 20));
        }

        @Test
        @DisplayName("Mês com 31 dias (jan): referência no último dia -> período jan 21 a fev 20")
        void month31_referenceLastDay_periodEndsNextMonth() {
            LocalDate ref = LocalDate.of(2023, 1, 31);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 1, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 2, 20));
        }

        @Test
        @DisplayName("Mês com 31 dias (mar): referência no meio -> fev 21 a mar 20")
        void month31_referenceMidMonth_periodFromPreviousMonth() {
            LocalDate ref = LocalDate.of(2023, 3, 15);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 2, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 3, 20));
        }

        @Test
        @DisplayName("Dia 21 exato: período inicia no mês atual e termina no próximo")
        void exactStartDay_periodStartsToday() {
            LocalDate ref = LocalDate.of(2023, 5, 21);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 5, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 6, 20));
        }

        @Test
        @DisplayName("Dia 20 exato: período termina hoje (início no mês anterior)")
        void exactEndDay_periodEndsToday() {
            LocalDate ref = LocalDate.of(2023, 6, 20);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, START_21, END_20);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 5, 21));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 6, 20));
        }
    }

    @Nested
    @DisplayName("Ajuste de dia inexistente no mês (clamp)")
    class DayClamping {

        @Test
        @DisplayName("Fim do período dia 31 em mês com 30 dias -> usa dia 30")
        void endDay31_inMonthWith30Days_clampsTo30() {
            // Período no mesmo mês (15-31): jun tem 30 dias, fim 31 é ajustado para 30
            LocalDate ref = LocalDate.of(2023, 6, 20);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, 15, 31);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 6, 15));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 6, 30));
        }

        @Test
        @DisplayName("Início do período dia 31 em mês com 30 dias -> usa dia 30")
        void startDay31_inMonthWith30Days_clampsTo30() {
            // Referência em abr, dia 10 -> período mar 31 - abr 30; mar tem 31, abr tem 30
            LocalDate ref = LocalDate.of(2023, 4, 10);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, 31, 30);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 3, 31));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 4, 30));
        }

        @Test
        @DisplayName("Dia 31 em fevereiro (não bissexto) -> usa 28")
        void day31_inFebruary_nonLeap_clampsTo28() {
            LocalDate ref = LocalDate.of(2023, 3, 15); // período fev 21 - mar 20; não usa 31
            // Testar config que force fev 31: período jan 31 - fev 31
            LocalDate refFeb = LocalDate.of(2023, 2, 15);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(refFeb, 31, 30);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 1, 31));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 2, 28));
        }
    }

    @Nested
    @DisplayName("Período no mesmo mês (startDay <= endDay)")
    class PeriodWithinSameMonth {

        @Test
        @DisplayName("Config 1 a 31: período do mês inteiro")
        void sameMonth_fullMonth_periodIsFirstToLast() {
            LocalDate ref = LocalDate.of(2023, 6, 15);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, 1, 31);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 6, 1));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 6, 30)); // jun tem 30
        }

        @Test
        @DisplayName("Config 1 a 31 em janeiro: fim em dia 31")
        void sameMonth_january_endDay31() {
            LocalDate ref = LocalDate.of(2023, 1, 15);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, 1, 31);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 1, 1));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 1, 31));
        }

        @Test
        @DisplayName("Config 5 a 15 no mesmo mês")
        void sameMonth_midRange() {
            LocalDate ref = LocalDate.of(2023, 7, 10);
            PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(ref, 5, 15);

            assertThat(bounds.getStart()).isEqualTo(LocalDate.of(2023, 7, 5));
            assertThat(bounds.getEnd()).isEqualTo(LocalDate.of(2023, 7, 15));
        }
    }
}

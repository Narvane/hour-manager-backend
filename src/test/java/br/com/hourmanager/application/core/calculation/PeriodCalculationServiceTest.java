package br.com.hourmanager.application.core.calculation;

import br.com.hourmanager.application.core.domains.HourEntry;
import br.com.hourmanager.application.core.period.PeriodBounds;
import br.com.hourmanager.application.ports.output.repositories.HourEntryRepository;
import br.com.hourmanager.application.ports.output.repositories.PeriodAdjustmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodCalculationServiceTest {

    private static final LocalDate START = LocalDate.of(2025, 1, 21);
    private static final LocalDate END = LocalDate.of(2025, 2, 20);
    private static final PeriodBounds BOUNDS = PeriodBounds.builder().start(START).end(END).build();

    @Mock
    private HourEntryRepository hourEntryRepository;

    @Mock
    private PeriodAdjustmentRepository periodAdjustmentRepository;

    private PeriodCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PeriodCalculationService(hourEntryRepository, periodAdjustmentRepository);
    }

    @Nested
    @DisplayName("Período vazio")
    class EmptyPeriod {

        @Test
        @DisplayName("Sem entradas nem ajustes -> totais e saldo zero")
        void noData_returnsZeros() {
            when(hourEntryRepository.findByEntryDateBetween(any(), any())).thenReturn(List.of());
            when(periodAdjustmentRepository.getAdjustment(any(), any())).thenReturn(Optional.empty());

            PeriodBalance result = service.compute(BOUNDS);

            assertThat(result.getTotalWorked()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getTotalAdjusted()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Apenas entradas de horas")
    class OnlyEntries {

        @Test
        @DisplayName("Uma entrada -> total trabalhado e saldo iguais")
        void singleEntry() {
            when(hourEntryRepository.findByEntryDateBetween(any(), any())).thenReturn(List.of(
                    entry(START, "8")
            ));
            when(periodAdjustmentRepository.getAdjustment(any(), any())).thenReturn(Optional.empty());

            PeriodBalance result = service.compute(BOUNDS);

            assertThat(result.getTotalWorked()).isEqualByComparingTo("8");
            assertThat(result.getTotalAdjusted()).isEqualByComparingTo("0");
            assertThat(result.getBalance()).isEqualByComparingTo("8");
        }

        @Test
        @DisplayName("Várias entradas -> soma correta")
        void multipleEntries() {
            when(hourEntryRepository.findByEntryDateBetween(any(), any())).thenReturn(List.of(
                    entry(LocalDate.of(2025, 1, 22), "8"),
                    entry(LocalDate.of(2025, 1, 23), "6.5"),
                    entry(LocalDate.of(2025, 2, 10), "8")
            ));
            when(periodAdjustmentRepository.getAdjustment(any(), any())).thenReturn(Optional.empty());

            PeriodBalance result = service.compute(BOUNDS);

            assertThat(result.getTotalWorked()).isEqualByComparingTo("22.5");
            assertThat(result.getTotalAdjusted()).isEqualByComparingTo("0");
            assertThat(result.getBalance()).isEqualByComparingTo("22.5");
        }
    }

    @Nested
    @DisplayName("Apenas ajustes")
    class OnlyAdjustments {

        @Test
        @DisplayName("Ajuste positivo (saldo inicial) -> total ajustado e saldo iguais")
        void singlePositiveAdjustment() {
            when(hourEntryRepository.findByEntryDateBetween(any(), any())).thenReturn(List.of());
            when(periodAdjustmentRepository.getAdjustment(any(), any())).thenReturn(Optional.of(new BigDecimal("40")));

            PeriodBalance result = service.compute(BOUNDS);

            assertThat(result.getTotalWorked()).isEqualByComparingTo("0");
            assertThat(result.getTotalAdjusted()).isEqualByComparingTo("40");
            assertThat(result.getBalance()).isEqualByComparingTo("40");
        }

        @Test
        @DisplayName("Ajuste negativo (correção)")
        void singleNegativeAdjustment() {
            when(hourEntryRepository.findByEntryDateBetween(any(), any())).thenReturn(List.of());
            when(periodAdjustmentRepository.getAdjustment(any(), any())).thenReturn(Optional.of(new BigDecimal("-2")));

            PeriodBalance result = service.compute(BOUNDS);

            assertThat(result.getTotalWorked()).isEqualByComparingTo("0");
            assertThat(result.getTotalAdjusted()).isEqualByComparingTo("-2");
            assertThat(result.getBalance()).isEqualByComparingTo("-2");
        }

        @Test
        @DisplayName("Vários ajustes (positivos e negativos)")
        void multipleAdjustments() {
            when(hourEntryRepository.findByEntryDateBetween(any(), any())).thenReturn(List.of());
            when(periodAdjustmentRepository.getAdjustment(any(), any())).thenReturn(Optional.of(new BigDecimal("38")));

            PeriodBalance result = service.compute(BOUNDS);

            assertThat(result.getTotalWorked()).isEqualByComparingTo("0");
            assertThat(result.getTotalAdjusted()).isEqualByComparingTo("38");
            assertThat(result.getBalance()).isEqualByComparingTo("38");
        }
    }

    @Nested
    @DisplayName("Entradas e ajustes combinados")
    class EntriesAndAdjustments {

        @Test
        @DisplayName("Entradas + ajustes -> saldo = total trabalhado + total ajustado")
        void combined() {
            when(hourEntryRepository.findByEntryDateBetween(any(), any())).thenReturn(List.of(
                    entry(LocalDate.of(2025, 1, 22), "8"),
                    entry(LocalDate.of(2025, 1, 23), "6.5"),
                    entry(LocalDate.of(2025, 2, 10), "8")
            ));
            when(periodAdjustmentRepository.getAdjustment(any(), any())).thenReturn(Optional.of(new BigDecimal("38")));

            PeriodBalance result = service.compute(BOUNDS);

            assertThat(result.getTotalWorked()).isEqualByComparingTo("22.5");
            assertThat(result.getTotalAdjusted()).isEqualByComparingTo("38");
            assertThat(result.getBalance()).isEqualByComparingTo("60.5");
        }

        @Test
        @DisplayName("Precisão decimal preservada")
        void decimalPrecision() {
            when(hourEntryRepository.findByEntryDateBetween(any(), any())).thenReturn(List.of(
                    entry(START, "7.25"),
                    entry(LocalDate.of(2025, 1, 22), "4.75")
            ));
            when(periodAdjustmentRepository.getAdjustment(any(), any())).thenReturn(Optional.of(new BigDecimal("0.5")));

            PeriodBalance result = service.compute(BOUNDS);

            assertThat(result.getTotalWorked()).isEqualByComparingTo("12");
            assertThat(result.getTotalAdjusted()).isEqualByComparingTo("0.5");
            assertThat(result.getBalance()).isEqualByComparingTo("12.5");
        }
    }

    @Nested
    @DisplayName("PeriodBalance.of")
    class PeriodBalanceFactory {

        @Test
        @DisplayName("balance é soma de totalWorked e totalAdjusted")
        void balanceIsSum() {
            PeriodBalance balance = PeriodBalance.of(new BigDecimal("10"), new BigDecimal("-1"));

            assertThat(balance.getTotalWorked()).isEqualByComparingTo("10");
            assertThat(balance.getTotalAdjusted()).isEqualByComparingTo("-1");
            assertThat(balance.getBalance()).isEqualByComparingTo("9");
        }
    }

    private static HourEntry entry(LocalDate date, String hours) {
        return HourEntry.builder()
                .id(UUID.randomUUID())
                .entryDate(date)
                .hours(new BigDecimal(hours))
                .description("")
                .build();
    }
}

package br.com.hourmanager.application.core.projection;

import br.com.hourmanager.application.core.calculation.PeriodBalance;
import br.com.hourmanager.application.core.calculation.PeriodCalculationResult;
import br.com.hourmanager.application.core.calculation.PeriodCalculationService;
import br.com.hourmanager.application.core.calculation.WeekInPeriod;
import br.com.hourmanager.application.core.period.PeriodBounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardProjectionServiceTest {

    private static final LocalDate START = LocalDate.of(2025, 1, 21);
    private static final LocalDate END = LocalDate.of(2025, 2, 20);
    private static final PeriodBounds BOUNDS = PeriodBounds.builder().start(START).end(END).build();

    @Mock
    private PeriodCalculationService periodCalculationService;

    private DashboardProjectionService service;

    @BeforeEach
    void setUp() {
        service = new DashboardProjectionService(periodCalculationService);
    }

    @Test
    @DisplayName("Monta projeção com período, totais, progresso e semanas")
    void project_assemblesFullProjection() {
        LocalDate reference = LocalDate.of(2025, 2, 10); // dentro do período
        WeekInPeriod week1 = WeekInPeriod.builder()
                .weekStart(LocalDate.of(2025, 1, 20))
                .weekEnd(LocalDate.of(2025, 1, 26))
                .totalWorked(new BigDecimal("8"))
                .totalAdjusted(BigDecimal.ZERO)
                .balance(new BigDecimal("8"))
                .workingDaysCount(0)
                .hoursAvailable(BigDecimal.ZERO)
                .baseWeeklyHours(BigDecimal.ZERO)
                .totalSegmentHours(new BigDecimal("168"))
                .build();
        when(periodCalculationService.computeWithWeeklyBreakdown(any(), any(), any())).thenReturn(
                PeriodCalculationResult.builder()
                        .summary(PeriodBalance.of(new BigDecimal("22.5"), new BigDecimal("38")))
                        .weeks(List.of(week1))
                        .build()
        );

        DashboardProjection projection = service.project(BOUNDS, reference, null, Set.of(), Set.of());

        assertThat(projection.getPeriod().getStart()).isEqualTo(START);
        assertThat(projection.getPeriod().getEnd()).isEqualTo(END);
        assertThat(projection.getPeriod().getTotalDays()).isEqualTo(31);

        assertThat(projection.getTotals().getTotalWorked()).isEqualByComparingTo("22.5");
        assertThat(projection.getTotals().getTotalAdjusted()).isEqualByComparingTo("38");
        assertThat(projection.getTotals().getBalance()).isEqualByComparingTo("60.5");

        assertThat(projection.getProgress().getDaysElapsed()).isEqualTo(21); // 21 jan a 10 fev inclusive
        assertThat(projection.getProgress().getTotalDays()).isEqualTo(31);
        assertThat(projection.getProgress().getPercentageElapsed()).isBetween(0.67, 0.68);

        assertThat(projection.getWeeks()).hasSize(1);
        assertThat(projection.getWeeks().get(0).getTotalWorked()).isEqualByComparingTo("8");
        assertThat(projection.getWeeks().get(0).getBalance()).isEqualByComparingTo("8");
        assertThat(projection.getWeeks().get(0).getDays()).hasSize(7);
    }

    @Test
    @DisplayName("Progresso 0 quando referência antes do período")
    void project_referenceBeforePeriod_progressZero() {
        when(periodCalculationService.computeWithWeeklyBreakdown(any(), any(), any())).thenReturn(
                PeriodCalculationResult.builder()
                        .summary(PeriodBalance.of(BigDecimal.ZERO, BigDecimal.ZERO))
                        .weeks(List.of())
                        .build()
        );

        DashboardProjection projection = service.project(BOUNDS, LocalDate.of(2025, 1, 15), null, Set.of(), Set.of());

        assertThat(projection.getProgress().getDaysElapsed()).isZero();
        assertThat(projection.getProgress().getPercentageElapsed()).isZero();
    }

    @Test
    @DisplayName("Progresso 1 quando referência após o período")
    void project_referenceAfterPeriod_progressOne() {
        when(periodCalculationService.computeWithWeeklyBreakdown(any(), any(), any())).thenReturn(
                PeriodCalculationResult.builder()
                        .summary(PeriodBalance.of(BigDecimal.ZERO, BigDecimal.ZERO))
                        .weeks(List.of())
                        .build()
        );

        DashboardProjection projection = service.project(BOUNDS, LocalDate.of(2025, 2, 25), null, Set.of(), Set.of());

        assertThat(projection.getProgress().getDaysElapsed()).isEqualTo(31);
        assertThat(projection.getProgress().getPercentageElapsed()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Com meta: projeção >= target -> ATINGIVEL")
    void project_withGoal_projectionMeetsTarget_atingivel() {
        LocalDate reference = LocalDate.of(2025, 2, 10); // 21 dias
        // 2 semanas * 40 = 80h meta. Ritmo 120/21 ≈ 5.71 → 5.71*31 ≈ 177 >= 80
        WeekInPeriod w1 = WeekInPeriod.builder().weekStart(START).weekEnd(END).totalWorked(BigDecimal.ZERO).totalAdjusted(BigDecimal.ZERO).balance(BigDecimal.ZERO).workingDaysCount(0).hoursAvailable(new BigDecimal("40")).baseWeeklyHours(new BigDecimal("40")).totalSegmentHours(new BigDecimal("744")).build();
        WeekInPeriod w2 = WeekInPeriod.builder().weekStart(START).weekEnd(END).totalWorked(BigDecimal.ZERO).totalAdjusted(BigDecimal.ZERO).balance(BigDecimal.ZERO).workingDaysCount(0).hoursAvailable(new BigDecimal("40")).baseWeeklyHours(new BigDecimal("40")).totalSegmentHours(new BigDecimal("744")).build();
        when(periodCalculationService.computeWithWeeklyBreakdown(any(), any(), any())).thenReturn(
                PeriodCalculationResult.builder()
                        .summary(PeriodBalance.of(new BigDecimal("120"), new BigDecimal("0")))
                        .weeks(List.of(w1, w2))
                        .build()
        );
        DashboardProjection projection = service.project(BOUNDS, reference, new BigDecimal("40"), Set.of(), Set.of());

        assertThat(projection.getGoalProjection()).isNotNull();
        assertThat(projection.getGoalProjection().getGoalStatus()).isEqualTo(GoalStatus.ATINGIVEL);
        assertThat(projection.getGoalProjection().getTargetHours()).isEqualByComparingTo("80");
        assertThat(projection.getGoalProjection().getProjectedBalanceAtEnd()).isGreaterThanOrEqualTo(new BigDecimal("80"));
    }

    @Test
    @DisplayName("Com meta: projeção entre 70% e 100% da meta -> EM_RISCO")
    void project_withGoal_projectionBetween70And100_emRisco() {
        LocalDate reference = LocalDate.of(2025, 2, 10);
        // Meta 2*40=80. Queremos projetar entre 56 e 80. 56/80=0.7. projected = rate*31, rate = balance/21. balance/21*31 = 0.75*80 = 60 → balance = 60*21/31 ≈ 40.6
        WeekInPeriod wa = WeekInPeriod.builder().weekStart(START).weekEnd(END).totalWorked(BigDecimal.ZERO).totalAdjusted(BigDecimal.ZERO).balance(BigDecimal.ZERO).workingDaysCount(0).hoursAvailable(new BigDecimal("40")).baseWeeklyHours(new BigDecimal("40")).totalSegmentHours(new BigDecimal("744")).build();
        WeekInPeriod wb = WeekInPeriod.builder().weekStart(START).weekEnd(END).totalWorked(BigDecimal.ZERO).totalAdjusted(BigDecimal.ZERO).balance(BigDecimal.ZERO).workingDaysCount(0).hoursAvailable(new BigDecimal("40")).baseWeeklyHours(new BigDecimal("40")).totalSegmentHours(new BigDecimal("744")).build();
        when(periodCalculationService.computeWithWeeklyBreakdown(any(), any(), any())).thenReturn(
                PeriodCalculationResult.builder()
                        .summary(PeriodBalance.of(new BigDecimal("42"), new BigDecimal("0")))
                        .weeks(List.of(wa, wb))
                        .build()
        );
        // projected ≈ 42/21*31 = 62. 62/80 = 0.775 → EM_RISCO
        DashboardProjection projection = service.project(BOUNDS, reference, new BigDecimal("40"), Set.of(), Set.of());

        assertThat(projection.getGoalProjection()).isNotNull();
        assertThat(projection.getGoalProjection().getGoalStatus()).isEqualTo(GoalStatus.EM_RISCO);
    }

    @Test
    @DisplayName("Com meta: projeção < 70% da meta -> IMPOSSIVEL")
    void project_withGoal_projectionBelow70_impossivel() {
        LocalDate reference = LocalDate.of(2025, 2, 10);
        WeekInPeriod wc = WeekInPeriod.builder().weekStart(START).weekEnd(END).totalWorked(BigDecimal.ZERO).totalAdjusted(BigDecimal.ZERO).balance(BigDecimal.ZERO).workingDaysCount(0).hoursAvailable(new BigDecimal("40")).baseWeeklyHours(new BigDecimal("40")).totalSegmentHours(new BigDecimal("744")).build();
        WeekInPeriod wd = WeekInPeriod.builder().weekStart(START).weekEnd(END).totalWorked(BigDecimal.ZERO).totalAdjusted(BigDecimal.ZERO).balance(BigDecimal.ZERO).workingDaysCount(0).hoursAvailable(new BigDecimal("40")).baseWeeklyHours(new BigDecimal("40")).totalSegmentHours(new BigDecimal("744")).build();
        when(periodCalculationService.computeWithWeeklyBreakdown(any(), any(), any())).thenReturn(
                PeriodCalculationResult.builder()
                        .summary(PeriodBalance.of(new BigDecimal("10"), new BigDecimal("0")))
                        .weeks(List.of(wc, wd))
                        .build()
        );
        // projected = 10/21*31 ≈ 14.76. Meta 80. 14.76/80 < 0.7
        DashboardProjection projection = service.project(BOUNDS, reference, new BigDecimal("40"), Set.of(), Set.of());

        assertThat(projection.getGoalProjection()).isNotNull();
        assertThat(projection.getGoalProjection().getGoalStatus()).isEqualTo(GoalStatus.IMPOSSIVEL);
    }

    @Test
    @DisplayName("Sem expectativa de horas: goalProjection é null")
    void project_withoutExpectedWeeklyHours_goalProjectionNull() {
        when(periodCalculationService.computeWithWeeklyBreakdown(any(), any(), any())).thenReturn(
                PeriodCalculationResult.builder()
                        .summary(PeriodBalance.of(new BigDecimal("22.5"), new BigDecimal("38")))
                        .weeks(List.of())
                        .build()
        );
        DashboardProjection projection = service.project(BOUNDS, LocalDate.of(2025, 2, 10), null, Set.of(), Set.of());
        assertThat(projection.getGoalProjection()).isNull();
    }
}

package br.com.hourmanager.adapters.output.repositories.protocols;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PeriodAdjustmentEntity")
@Table(name = "period_adjustments")
@IdClass(PeriodAdjustmentEntity.PeriodAdjustmentId.class)
public class PeriodAdjustmentEntity {

    @Id
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Id
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "adjusted_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustedHours;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodAdjustmentId implements java.io.Serializable {
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }
}

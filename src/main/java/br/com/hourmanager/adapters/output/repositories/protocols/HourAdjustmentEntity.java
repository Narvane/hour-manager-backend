package br.com.hourmanager.adapters.output.repositories.protocols;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "HourAdjustmentEntity")
@Table(name = "hour_adjustments")
public class HourAdjustmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "adjustment_date", nullable = false)
    private LocalDate adjustmentDate;

    @Column(name = "delta_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal deltaHours;

    @Column(length = 500)
    private String description;
}

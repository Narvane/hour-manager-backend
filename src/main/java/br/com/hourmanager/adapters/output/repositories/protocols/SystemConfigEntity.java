package br.com.hourmanager.adapters.output.repositories.protocols;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "SystemConfigEntity")
@Table(name = "system_config")
public class SystemConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "closure_start_day", nullable = false)
    private int closureStartDay;

    @Column(name = "closure_end_day", nullable = false)
    private int closureEndDay;

    @Column(name = "expected_weekly_hours", precision = 10, scale = 2)
    private BigDecimal expectedWeeklyHours;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

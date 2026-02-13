package br.com.hourmanager.application.core.domains;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ajuste de horas (filler).
 * Altera o saldo diretamente; usado para correções e início no meio do período.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HourAdjustment {

    private UUID id;
    private LocalDate adjustmentDate;
    private BigDecimal deltaHours;
    private String description;
}

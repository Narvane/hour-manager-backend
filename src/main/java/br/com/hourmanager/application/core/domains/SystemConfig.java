package br.com.hourmanager.application.core.domains;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Configuração estrutural de fechamento de período.
 * Única configuração ativa no sistema.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

    private UUID id;
    private int closureStartDay;
    private int closureEndDay;
    /** Expectativa de horas por semana (ex.: 40); opcional, usado para projeção da meta. */
    private BigDecimal expectedWeeklyHours;
    private Instant createdAt;
}

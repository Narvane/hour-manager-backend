package br.com.hourmanager.application.core.domains;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada de horas registrada pelo usuário.
 * Pertence conceitualmente a um período (calculado a partir da data + configuração).
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HourEntry {

    private UUID id;
    private LocalDate entryDate;
    private BigDecimal hours;
    private String description;
}

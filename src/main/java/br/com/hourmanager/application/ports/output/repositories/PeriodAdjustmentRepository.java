package br.com.hourmanager.application.ports.output.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Ajuste de horas por período (um valor por fechamento).
 * Não tem data nem descrição; não aparece nas barras semanais.
 */
public interface PeriodAdjustmentRepository {

    Optional<BigDecimal> getAdjustment(LocalDate periodStart, LocalDate periodEnd);

    void setAdjustment(LocalDate periodStart, LocalDate periodEnd, BigDecimal adjustedHours);
}

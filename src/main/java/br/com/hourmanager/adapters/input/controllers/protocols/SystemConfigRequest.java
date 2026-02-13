package br.com.hourmanager.adapters.input.controllers.protocols;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemConfigRequest {

    @Min(1)
    @Max(31)
    private int closureStartDay;

    @Min(1)
    @Max(31)
    private int closureEndDay;

    /** Expectativa de horas por semana (ex.: 40); opcional para projeção da meta. */
    @DecimalMin("0.01")
    private BigDecimal expectedWeeklyHours;
}

package br.com.hourmanager.adapters.input.controllers.protocols;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HourAdjustmentRequest {

    @NotNull(message = "adjustmentDate is required")
    private LocalDate adjustmentDate;

    @NotNull(message = "deltaHours is required")
    private BigDecimal deltaHours;

    @AssertTrue(message = "deltaHours must not be zero")
    public boolean isDeltaNonZero() {
        return deltaHours != null && deltaHours.compareTo(BigDecimal.ZERO) != 0;
    }

    @Size(max = 500)
    private String description;
}

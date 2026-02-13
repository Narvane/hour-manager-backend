package br.com.hourmanager.adapters.input.controllers.protocols;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
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
public class HourEntryRequest {

    @NotNull(message = "entryDate is required")
    private LocalDate entryDate;

    @NotNull(message = "hours is required")
    @DecimalMin(value = "0.01", message = "hours must be positive")
    private BigDecimal hours;

    @Size(max = 500)
    private String description;
}

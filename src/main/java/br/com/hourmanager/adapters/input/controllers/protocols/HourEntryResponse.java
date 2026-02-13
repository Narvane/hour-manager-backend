package br.com.hourmanager.adapters.input.controllers.protocols;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class HourEntryResponse {

    private String id;
    private LocalDate entryDate;
    private BigDecimal hours;
    private String description;
}

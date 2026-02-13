package br.com.hourmanager.adapters.input.controllers.protocols;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class PeriodBalanceResponse {

    private String periodStart;
    private String periodEnd;
    private BigDecimal totalWorked;
    private BigDecimal totalAdjusted;
    private BigDecimal balance;
}

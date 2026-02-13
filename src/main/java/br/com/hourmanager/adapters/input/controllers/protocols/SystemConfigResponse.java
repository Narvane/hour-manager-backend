package br.com.hourmanager.adapters.input.controllers.protocols;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemConfigResponse {

    private String id;
    private int closureStartDay;
    private int closureEndDay;
    private BigDecimal expectedWeeklyHours;
    private Instant createdAt;
}

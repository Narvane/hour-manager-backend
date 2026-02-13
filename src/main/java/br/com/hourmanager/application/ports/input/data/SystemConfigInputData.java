package br.com.hourmanager.application.ports.input.data;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class SystemConfigInputData {
    private int closureStartDay;
    private int closureEndDay;
    private BigDecimal expectedWeeklyHours;
}

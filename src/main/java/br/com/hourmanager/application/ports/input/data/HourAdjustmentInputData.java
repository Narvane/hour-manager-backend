package br.com.hourmanager.application.ports.input.data;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
public class HourAdjustmentInputData {
    private LocalDate adjustmentDate;
    private BigDecimal deltaHours;
    private String description;
}

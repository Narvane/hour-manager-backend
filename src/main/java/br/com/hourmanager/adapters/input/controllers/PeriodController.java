package br.com.hourmanager.adapters.input.controllers;

import br.com.hourmanager.adapters.input.controllers.protocols.PeriodBalanceResponse;
import br.com.hourmanager.adapters.input.controllers.protocols.PeriodCurrentResponse;
import br.com.hourmanager.application.core.calculation.PeriodBalance;
import br.com.hourmanager.application.core.calculation.PeriodCalculationService;
import br.com.hourmanager.application.core.period.ClosurePeriodEngine;
import br.com.hourmanager.application.core.period.PeriodBounds;
import br.com.hourmanager.application.ports.output.repositories.SystemConfigRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/period")
public class PeriodController {

    private final SystemConfigRepository systemConfigRepository;
    private final PeriodCalculationService periodCalculationService;

    public PeriodController(SystemConfigRepository systemConfigRepository,
                            PeriodCalculationService periodCalculationService) {
        this.systemConfigRepository = systemConfigRepository;
        this.periodCalculationService = periodCalculationService;
    }

    /**
     * Retorna o período atual (ou para a data informada) com base na configuração de fechamento.
     * Útil para validar manualmente a engine de período.
     */
    @GetMapping("/current")
    public ResponseEntity<PeriodCurrentResponse> getCurrent(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return systemConfigRepository.findCurrent()
                .map(config -> {
                    LocalDate reference = date != null ? date : LocalDate.now();
                    PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(
                            reference,
                            config.getClosureStartDay(),
                            config.getClosureEndDay()
                    );
                    return ResponseEntity.ok(
                            PeriodCurrentResponse.builder()
                                    .start(bounds.getStart().toString())
                                    .end(bounds.getEnd().toString())
                                    .build()
                    );
                })
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Cálculo derivado do período atual: total trabalhado, total ajustado e saldo.
     * Nada é persistido; resultado sempre calculado a partir de entradas e ajustes.
     */
    @GetMapping("/balance")
    public ResponseEntity<PeriodBalanceResponse> getCurrentBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return systemConfigRepository.findCurrent()
                .map(config -> {
                    LocalDate reference = date != null ? date : LocalDate.now();
                    PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(
                            reference,
                            config.getClosureStartDay(),
                            config.getClosureEndDay()
                    );
                    PeriodBalance balance = periodCalculationService.compute(bounds);
                    return ResponseEntity.ok(
                            PeriodBalanceResponse.builder()
                                    .periodStart(bounds.getStart().toString())
                                    .periodEnd(bounds.getEnd().toString())
                                    .totalWorked(balance.getTotalWorked())
                                    .totalAdjusted(balance.getTotalAdjusted())
                                    .balance(balance.getBalance())
                                    .build()
                    );
                })
                .orElse(ResponseEntity.noContent().build());
    }
}

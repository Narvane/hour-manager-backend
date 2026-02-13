package br.com.hourmanager.adapters.input.controllers;

import br.com.hourmanager.application.core.holidays.BrazilianHolidayProvider;
import br.com.hourmanager.application.core.period.ClosurePeriodEngine;
import br.com.hourmanager.application.core.period.PeriodBounds;
import br.com.hourmanager.application.core.projection.DashboardProjection;
import br.com.hourmanager.application.core.projection.DashboardProjectionService;
import br.com.hourmanager.application.ports.output.repositories.HolidayOverrideRepository;
import br.com.hourmanager.application.ports.output.repositories.SystemConfigRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Projeção do dashboard e overrides de feriados.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final SystemConfigRepository systemConfigRepository;
    private final DashboardProjectionService dashboardProjectionService;
    private final HolidayOverrideRepository holidayOverrideRepository;

    public DashboardController(SystemConfigRepository systemConfigRepository,
                               DashboardProjectionService dashboardProjectionService,
                               HolidayOverrideRepository holidayOverrideRepository) {
        this.systemConfigRepository = systemConfigRepository;
        this.dashboardProjectionService = dashboardProjectionService;
        this.holidayOverrideRepository = holidayOverrideRepository;
    }

    /**
     * Retorna a projeção completa: período, totais, progresso, semanas com dias (S T Q Q S (S)(D)) e feriados.
     */
    @GetMapping("/projection")
    public ResponseEntity<DashboardProjection> getProjection(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return systemConfigRepository.findCurrent()
                .map(config -> {
                    LocalDate reference = date != null ? date : LocalDate.now();
                    PeriodBounds bounds = ClosurePeriodEngine.computePeriodContaining(
                            reference,
                            config.getClosureStartDay(),
                            config.getClosureEndDay()
                    );
                    Set<LocalDate> effectiveHolidays = new HashSet<>(
                            BrazilianHolidayProvider.getHolidaysBetween(bounds.getStart(), bounds.getEnd()));
                    Map<LocalDate, Boolean> overrides = holidayOverrideRepository.getOverridesBetween(
                            bounds.getStart(), bounds.getEnd());
                    overrides.forEach((d, isHoliday) -> {
                        if (isHoliday) effectiveHolidays.add(d);
                        else effectiveHolidays.remove(d);
                    });
                    Set<LocalDate> overrideDates = new HashSet<>(overrides.keySet());
                    DashboardProjection projection = dashboardProjectionService.project(
                            bounds, reference, config.getExpectedWeeklyHours(),
                            effectiveHolidays, overrideDates);
                    return ResponseEntity.ok(projection);
                })
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Alterna override de feriado para uma data: persiste apenas o override; projeção recalcula ao refetch.
     * Body: { "date": "2025-01-21", "isHoliday": true } (true = feriado, false = dia útil).
     */
    @PatchMapping("/holiday-overrides")
    public ResponseEntity<DashboardProjection> toggleHolidayOverride(@RequestBody HolidayOverrideRequest request) {
        if (request.getDate() == null) {
            return ResponseEntity.badRequest().build();
        }
        holidayOverrideRepository.setOverride(request.getDate(), request.isHoliday());
        return getProjection(null);
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HolidayOverrideRequest {
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
        private java.time.LocalDate date;
        private boolean holiday;
    }
}

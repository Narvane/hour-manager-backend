package br.com.hourmanager.config;

import br.com.hourmanager.adapters.output.repositories.HolidayOverrideJpaAdapter;
import br.com.hourmanager.adapters.output.repositories.HourAdjustmentJpaAdapter;
import br.com.hourmanager.adapters.output.repositories.HourEntryJpaAdapter;
import br.com.hourmanager.adapters.output.repositories.SystemConfigJpaAdapter;
import br.com.hourmanager.adapters.output.repositories.jpa.HolidayOverrideJpaRepository;
import br.com.hourmanager.adapters.output.repositories.jpa.HourAdjustmentJpaRepository;
import br.com.hourmanager.adapters.output.repositories.jpa.HourEntryJpaRepository;
import br.com.hourmanager.adapters.output.repositories.jpa.SystemConfigJpaRepository;
import br.com.hourmanager.application.core.calculation.PeriodCalculationService;
import br.com.hourmanager.application.core.projection.DashboardProjectionService;
import br.com.hourmanager.application.core.usecases.CreateHourAdjustmentUseCase;
import br.com.hourmanager.application.core.usecases.CreateHourEntryUseCase;
import br.com.hourmanager.application.core.usecases.SaveSystemConfigUseCase;
import br.com.hourmanager.application.ports.input.CreateHourAdjustmentInputGateway;
import br.com.hourmanager.application.ports.input.CreateHourEntryInputGateway;
import br.com.hourmanager.application.ports.input.SystemConfigInputGateway;
import br.com.hourmanager.application.ports.output.repositories.HolidayOverrideRepository;
import br.com.hourmanager.application.ports.output.repositories.HourAdjustmentRepository;
import br.com.hourmanager.application.ports.output.repositories.HourEntryRepository;
import br.com.hourmanager.application.ports.output.repositories.SystemConfigRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HourManagerConfig {

    @Bean
    public SystemConfigRepository systemConfigRepository(SystemConfigJpaRepository jpaRepository) {
        return new SystemConfigJpaAdapter(jpaRepository);
    }

    @Bean
    public HolidayOverrideRepository holidayOverrideRepository(HolidayOverrideJpaRepository jpaRepository) {
        return new HolidayOverrideJpaAdapter(jpaRepository);
    }

    @Bean
    public HourEntryRepository hourEntryRepository(HourEntryJpaRepository jpaRepository) {
        return new HourEntryJpaAdapter(jpaRepository);
    }

    @Bean
    public HourAdjustmentRepository hourAdjustmentRepository(HourAdjustmentJpaRepository jpaRepository) {
        return new HourAdjustmentJpaAdapter(jpaRepository);
    }

    @Bean
    public SystemConfigInputGateway saveSystemConfigUseCase(SystemConfigRepository systemConfigRepository) {
        return new SaveSystemConfigUseCase(systemConfigRepository);
    }

    @Bean
    public CreateHourEntryInputGateway createHourEntryUseCase(HourEntryRepository hourEntryRepository) {
        return new CreateHourEntryUseCase(hourEntryRepository);
    }

    @Bean
    public CreateHourAdjustmentInputGateway createHourAdjustmentUseCase(HourAdjustmentRepository hourAdjustmentRepository) {
        return new CreateHourAdjustmentUseCase(hourAdjustmentRepository);
    }

    @Bean
    public PeriodCalculationService periodCalculationService(HourEntryRepository hourEntryRepository,
                                                            HourAdjustmentRepository hourAdjustmentRepository) {
        return new PeriodCalculationService(hourEntryRepository, hourAdjustmentRepository);
    }

    @Bean
    public DashboardProjectionService dashboardProjectionService(PeriodCalculationService periodCalculationService) {
        return new DashboardProjectionService(periodCalculationService);
    }
}

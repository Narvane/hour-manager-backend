package br.com.hourmanager.application.core.usecases;

import br.com.hourmanager.application.core.domains.HourAdjustment;
import br.com.hourmanager.application.ports.input.CreateHourAdjustmentInputGateway;
import br.com.hourmanager.application.ports.input.data.HourAdjustmentInputData;
import br.com.hourmanager.application.ports.output.repositories.HourAdjustmentRepository;

public class CreateHourAdjustmentUseCase implements CreateHourAdjustmentInputGateway {

    private final HourAdjustmentRepository hourAdjustmentRepository;

    public CreateHourAdjustmentUseCase(HourAdjustmentRepository hourAdjustmentRepository) {
        this.hourAdjustmentRepository = hourAdjustmentRepository;
    }

    @Override
    public HourAdjustment create(HourAdjustmentInputData data) {
        HourAdjustment adjustment = HourAdjustment.builder()
                .adjustmentDate(data.getAdjustmentDate())
                .deltaHours(data.getDeltaHours())
                .description(data.getDescription())
                .build();
        return hourAdjustmentRepository.save(adjustment);
    }
}

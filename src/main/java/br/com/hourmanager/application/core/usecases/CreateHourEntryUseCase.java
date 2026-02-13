package br.com.hourmanager.application.core.usecases;

import br.com.hourmanager.application.core.domains.HourEntry;
import br.com.hourmanager.application.ports.input.CreateHourEntryInputGateway;
import br.com.hourmanager.application.ports.input.data.HourEntryInputData;
import br.com.hourmanager.application.ports.output.repositories.HourEntryRepository;

public class CreateHourEntryUseCase implements CreateHourEntryInputGateway {

    private final HourEntryRepository hourEntryRepository;

    public CreateHourEntryUseCase(HourEntryRepository hourEntryRepository) {
        this.hourEntryRepository = hourEntryRepository;
    }

    @Override
    public HourEntry create(HourEntryInputData data) {
        HourEntry entry = HourEntry.builder()
                .entryDate(data.getEntryDate())
                .hours(data.getHours())
                .description(data.getDescription())
                .build();
        return hourEntryRepository.save(entry);
    }
}

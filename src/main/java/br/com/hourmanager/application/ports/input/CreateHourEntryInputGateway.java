package br.com.hourmanager.application.ports.input;

import br.com.hourmanager.application.core.domains.HourEntry;
import br.com.hourmanager.application.ports.input.data.HourEntryInputData;

public interface CreateHourEntryInputGateway {

    HourEntry create(HourEntryInputData data);
}

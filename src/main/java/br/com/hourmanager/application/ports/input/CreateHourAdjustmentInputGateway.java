package br.com.hourmanager.application.ports.input;

import br.com.hourmanager.application.core.domains.HourAdjustment;
import br.com.hourmanager.application.ports.input.data.HourAdjustmentInputData;

public interface CreateHourAdjustmentInputGateway {

    HourAdjustment create(HourAdjustmentInputData data);
}

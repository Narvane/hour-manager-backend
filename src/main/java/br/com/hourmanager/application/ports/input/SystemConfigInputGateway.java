package br.com.hourmanager.application.ports.input;

import br.com.hourmanager.application.ports.input.data.SystemConfigInputData;

public interface SystemConfigInputGateway {

    void save(SystemConfigInputData data);
}

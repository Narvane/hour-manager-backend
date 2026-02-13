package br.com.hourmanager.application.ports.output.repositories;

import br.com.hourmanager.application.core.domains.SystemConfig;

import java.util.Optional;

public interface SystemConfigRepository {

    Optional<SystemConfig> findCurrent();

    SystemConfig save(SystemConfig systemConfig);
}

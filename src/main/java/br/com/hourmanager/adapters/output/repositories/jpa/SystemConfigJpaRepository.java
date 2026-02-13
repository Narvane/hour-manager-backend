package br.com.hourmanager.adapters.output.repositories.jpa;

import br.com.hourmanager.adapters.output.repositories.protocols.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigEntity, UUID> {
}

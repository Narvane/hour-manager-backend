package br.com.hourmanager.adapters.output.repositories.jpa;

import br.com.hourmanager.adapters.output.repositories.protocols.HolidayOverrideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HolidayOverrideJpaRepository extends JpaRepository<HolidayOverrideEntity, UUID> {

    List<HolidayOverrideEntity> findByOverrideDateBetweenOrderByOverrideDateAsc(LocalDate start, LocalDate end);

    Optional<HolidayOverrideEntity> findByOverrideDate(LocalDate date);
}

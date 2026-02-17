package br.com.hourmanager.adapters.output.repositories.jpa;

import br.com.hourmanager.adapters.output.repositories.protocols.HourEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HourEntryJpaRepository extends JpaRepository<HourEntryEntity, UUID> {

    List<HourEntryEntity> findByEntryDateBetweenOrderByEntryDateAsc(LocalDate start, LocalDate end);

    Page<HourEntryEntity> findByEntryDateBetweenOrderByEntryDateDesc(LocalDate start, LocalDate end, Pageable pageable);
}

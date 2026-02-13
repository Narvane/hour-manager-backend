package br.com.hourmanager.adapters.output.repositories.protocols;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Override manual: usuário marca/desmarca um dia como feriado.
 * Persistimos apenas estes overrides; feriados nacionais vêm do BrazilianHolidayProvider.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "HolidayOverrideEntity")
@Table(name = "holiday_overrides", uniqueConstraints = @UniqueConstraint(columnNames = "override_date"))
public class HolidayOverrideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "override_date", nullable = false, unique = true)
    private LocalDate overrideDate;

    /** true = tratar como feriado; false = tratar como dia útil (ex.: desmarcar feriado nacional). */
    @Column(name = "is_holiday", nullable = false)
    private boolean holiday;
}

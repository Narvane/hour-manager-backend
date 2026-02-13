package br.com.hourmanager.adapters.output.repositories.protocols;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "HourEntryEntity")
@Table(name = "hour_entries")
public class HourEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours;

    @Column(length = 500)
    private String description;
}

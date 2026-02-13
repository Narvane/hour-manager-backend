package br.com.hourmanager.application.core.calculation;

import br.com.hourmanager.application.core.period.PeriodBounds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodWeekSegmentsTest {

    @Test
    @DisplayName("Período 21 jan – 20 fev gera segmentos recortados: 21–25 jan, 26 jan–1 fev, 2–8 fev, 9–15 fev, 16–20 fev")
    void segmentsWithin_period21JanTo20Feb_producesFiveSegments() {
        PeriodBounds bounds = PeriodBounds.builder()
                .start(LocalDate.of(2025, 1, 21))
                .end(LocalDate.of(2025, 2, 20))
                .build();

        List<PeriodWeekSegments.SegmentBounds> segments = PeriodWeekSegments.segmentsWithin(bounds);

        assertThat(segments).hasSize(5);
        assertThat(segments.get(0).start()).isEqualTo(LocalDate.of(2025, 1, 21));
        assertThat(segments.get(0).end()).isEqualTo(LocalDate.of(2025, 1, 25));

        assertThat(segments.get(1).start()).isEqualTo(LocalDate.of(2025, 1, 26));
        assertThat(segments.get(1).end()).isEqualTo(LocalDate.of(2025, 2, 1));

        assertThat(segments.get(2).start()).isEqualTo(LocalDate.of(2025, 2, 2));
        assertThat(segments.get(2).end()).isEqualTo(LocalDate.of(2025, 2, 8));

        assertThat(segments.get(3).start()).isEqualTo(LocalDate.of(2025, 2, 9));
        assertThat(segments.get(3).end()).isEqualTo(LocalDate.of(2025, 2, 15));

        assertThat(segments.get(4).start()).isEqualTo(LocalDate.of(2025, 2, 16));
        assertThat(segments.get(4).end()).isEqualTo(LocalDate.of(2025, 2, 20));
    }
}

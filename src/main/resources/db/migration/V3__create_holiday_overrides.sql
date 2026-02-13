CREATE TABLE holiday_overrides
(
    id            UUID PRIMARY KEY,
    override_date DATE    NOT NULL,
    is_holiday    BOOLEAN NOT NULL,
    CONSTRAINT uq_holiday_overrides_date UNIQUE (override_date)
);

CREATE INDEX idx_holiday_overrides_date ON holiday_overrides (override_date);
